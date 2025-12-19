package org.example.service;

import org.example.entity.Friend;
import org.example.entity.Friend.FriendStatus;
import org.example.entity.User;
import org.example.exception.customs.httpstatus.BadRequestException;
import org.example.exception.customs.httpstatus.ConflictException;
import org.example.exception.customs.httpstatus.ForbiddenException;
import org.example.exception.customs.httpstatus.NotFoundException;
import org.example.repository.FriendRepository;
import org.example.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepo;
    private final UserRepository userRepo;
    private final AuthService authService;

    public void sendRequest(UUID receiverId) {

        UUID requesterId = authService.getCurrentUserId();

        if (requesterId.equals(receiverId)) {
            throw new BadRequestException("No puedes enviarte una solicitud a ti mismo");
        }

        User requester = userRepo.findById(requesterId)
                .orElseThrow(() -> new NotFoundException("Requester no encontrado"));
        User receiver = userRepo.findById(receiverId)
                .orElseThrow(() -> new NotFoundException("Receiver no encontrado"));

        Optional<Friend> existing = friendRepo.findByRequesterAndReceiverOrRequesterAndReceiver(
                requester, receiver,
                receiver, requester);

        if (existing.isPresent()) {
            Friend relation = existing.get();
            if (relation.getStatus() == FriendStatus.BLOCKED) {
                throw new ConflictException("El usuario está bloqueado, no se puede enviar solicitud");
            }
            if (relation.getStatus() == FriendStatus.PENDING) {
                throw new ConflictException("Ya existe una solicitud pendiente");
            }
            if (relation.getStatus() == FriendStatus.ACCEPTED) {
                throw new ConflictException("Ya son amigos");
            }
            if (relation.getStatus() == FriendStatus.REJECTED) {
                // permitir reintentar → se actualiza a PENDING
                relation.setRequester(requester);
                relation.setReceiver(receiver);
                relation.setStatus(FriendStatus.PENDING);
                friendRepo.save(relation);
                return;
            }
        }

        // Crear nueva relación
        Friend friend = Friend.builder()
                .requester(requester)
                .receiver(receiver)
                .status(FriendStatus.PENDING)
                .build();
        friendRepo.save(friend);
    }

    @CacheEvict(value = { "feeds", "posts", "postsByUser" }, allEntries = true)
    public void acceptRequest(UUID requesterId) {
        UUID receiverId = authService.getCurrentUserId();

        Friend relation = friendRepo.findByRequesterIdAndReceiverId(requesterId, receiverId)
                .orElseThrow(() -> new NotFoundException("Solicitud no encontrada"));

        if (relation.getStatus() != FriendStatus.PENDING) {
            throw new ConflictException("No se puede aceptar esta solicitud");
        }
        relation.setStatus(FriendStatus.ACCEPTED);
        friendRepo.save(relation);
    }

    public void rejectRequest(UUID requesterId) {
        UUID receiverId = authService.getCurrentUserId();

        Friend relation = friendRepo.findByRequesterIdAndReceiverId(requesterId, receiverId)
                .orElseThrow(() -> new NotFoundException("Solicitud no encontrada"));
        if (relation.getStatus() != FriendStatus.PENDING) {
            throw new ConflictException("No se puede rechazar esta solicitud");
        }
        relation.setStatus(FriendStatus.REJECTED);
        friendRepo.save(relation);
    }

    public void cancelRequest(UUID receiverId) {
        UUID requesterId = authService.getCurrentUserId();

        Friend relation = friendRepo.findByRequesterIdAndReceiverId(requesterId, receiverId)
                .orElseThrow(() -> new NotFoundException("Solicitud no encontrada"));

        if (relation.getStatus() != FriendStatus.PENDING) {
            throw new ConflictException("Solo se pueden cancelar solicitudes pendientes");
        }
        friendRepo.delete(relation); // se elimina la relación
    }

    @CacheEvict(value = { "feeds", "posts", "postsByUser" }, allEntries = true)
    public void removeFriend(UUID userId2) {
        UUID userId1 = authService.getCurrentUserId();

        Optional<Friend> relation = friendRepo.findByRequesterAndReceiverOrRequesterAndReceiver(
                userRepo.getReferenceById(userId1),
                userRepo.getReferenceById(userId2),
                userRepo.getReferenceById(userId2),
                userRepo.getReferenceById(userId1));

        if (relation.isEmpty() || relation.get().getStatus() != FriendStatus.ACCEPTED) {
            throw new ConflictException("No son amigos");
        }
        friendRepo.delete(relation.get());
    }

    @CacheEvict(value = { "feeds", "posts", "postsByUser" }, allEntries = true)
    public void blockUser(UUID blockedId) {

        UUID blockerId = authService.getCurrentUserId();

        User blocker = userRepo.findById(blockerId)
                .orElseThrow(() -> new NotFoundException("Blocker no encontrado"));
        User blocked = userRepo.findById(blockedId)
                .orElseThrow(() -> new NotFoundException("Blocked no encontrado"));

        Optional<Friend> existing = friendRepo.findByRequesterAndReceiverOrRequesterAndReceiver(
                blocker, blocked,
                blocked, blocker);

        Friend relation;
        if (existing.isPresent()) {
            relation = existing.get();
            relation.setRequester(blocker);
            relation.setReceiver(blocked);
            relation.setStatus(FriendStatus.BLOCKED);
        } else {
            relation = Friend.builder()
                    .requester(blocker)
                    .receiver(blocked)
                    .status(FriendStatus.BLOCKED)
                    .build();
        }
        friendRepo.save(relation);
    }

    // Validacion ¿Este usuario puede acceder a este chat?
    public Friend getAcceptedFriendForUser(Long friendId, User user) {
        Friend friend = friendRepo.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Chat no encontrado"));

        if (friend.getStatus() != Friend.FriendStatus.ACCEPTED) {
            throw new ForbiddenException("Chat no disponible");
        }

        boolean participant = friend.getRequester().getId().equals(user.getId()) ||
                friend.getReceiver().getId().equals(user.getId());

        if (!participant) {
            throw new ForbiddenException("No perteneces a este chat");
        }

        return friend;
    }

    public List<Friend> getAllAcceptedFriends(User user) {
        return friendRepo.findAllByUserAndStatus(
                user,
                Friend.FriendStatus.ACCEPTED);
    }
}

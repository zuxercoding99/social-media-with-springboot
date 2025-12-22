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

        Optional<Friend> existing = friendRepo.findRelationBetween(requester, receiver);

        if (existing.isPresent()) {
            Friend relation = existing.get();

            switch (relation.getStatus()) {

                case BLOCKED ->
                    throw new ConflictException("El usuario está bloqueado");

                case ACCEPTED ->
                    throw new ConflictException("Ya son amigos");

                case PENDING -> {
                    // 🔐 Diferenciar dirección
                    if (relation.getRequester().getId().equals(requesterId)) {
                        // yo ya envié
                        throw new ConflictException("Ya enviaste una solicitud");
                    } else {
                        // 🔥 el otro me envió → auto-aceptar
                        relation.setStatus(FriendStatus.ACCEPTED);
                        friendRepo.save(relation);
                        return;
                    }
                }

                case REJECTED -> {
                    // reintentar
                    relation.setRequester(requester);
                    relation.setReceiver(receiver);
                    relation.setStatus(FriendStatus.PENDING);
                    friendRepo.save(relation);
                    return;
                }
            }
        }

        // Nueva solicitud
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

        Friend relation = friendRepo
                .findPendingRequest(requesterId, receiverId)
                .orElseThrow(() -> new NotFoundException("Solicitud no encontrada"));

        relation.setStatus(FriendStatus.ACCEPTED);
        friendRepo.save(relation);
    }

    public void rejectRequest(UUID requesterId) {
        UUID receiverId = authService.getCurrentUserId();

        Friend relation = friendRepo
                .findPendingRequest(requesterId, receiverId)
                .orElseThrow(() -> new NotFoundException("Solicitud no encontrada"));

        relation.setStatus(FriendStatus.REJECTED);
        friendRepo.save(relation);
    }

    public void cancelRequest(UUID receiverId) {
        UUID requesterId = authService.getCurrentUserId();

        Friend relation = friendRepo
                .findPendingRequest(requesterId, receiverId)
                .orElseThrow(() -> new NotFoundException(
                        "No existe una solicitud enviada por este usuario"));

        friendRepo.delete(relation); // se elimina la relación
    }

    @CacheEvict(value = { "feeds", "posts", "postsByUser" }, allEntries = true)
    public void removeFriend(UUID userId2) {
        UUID userId1 = authService.getCurrentUserId();

        Optional<Friend> relation = friendRepo.findRelationBetweenUserIds(userId1, userId2);

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

        Optional<Friend> existing = friendRepo.findRelationBetween(blocker, blocked);

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
        return friendRepo.findAllByFriendRelationsOfUserByStatus(
                user,
                Friend.FriendStatus.ACCEPTED);
    }
}

package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.FriendDto;
import org.example.dto.FriendRequestDto;
import org.example.dto.UserProfileDto;
import org.example.entity.Friend;
import org.example.entity.User;
import org.example.exception.customs.httpstatus.NotFoundException;
import org.example.repository.FriendRepository;
import org.example.repository.PostRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

        private final UserRepository userRepository;
        private final FriendRepository friendRepository;
        private final AuthService authService;
        private final PostRepository postRepository;

        // ----------------- PERFIL PÚBLICO -----------------

        // Obtener perfil público
        public UserProfileDto getUserProfile(String username) {

                User currentUser = authService.getCurrentUser();

                User profileUser = userRepository.findByUsernameIgnoreCase(username.trim())
                                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

                boolean isFriend = friendRepository
                                .findByRequesterAndReceiverOrRequesterAndReceiver(
                                                currentUser, profileUser, profileUser, currentUser)
                                .map(rel -> rel.getStatus() == Friend.FriendStatus.ACCEPTED)
                                .orElse(false);

                boolean requestPending = friendRepository
                                .findByRequesterAndReceiver(profileUser, currentUser)
                                .map(rel -> rel.getStatus() == Friend.FriendStatus.PENDING)
                                .orElse(false);

                long postCount = postRepository.countByUserId(profileUser.getId());
                long friendCount = friendRepository.countFriends(profileUser.getId());

                String avatarUrl = "/api/v1/avatars/" + profileUser.getAvatarKey();
                String bio = Optional.ofNullable(profileUser.getBio()).orElse("");

                return new UserProfileDto(
                                profileUser.getId(),
                                profileUser.getUsername(),
                                profileUser.getDisplayName(),
                                avatarUrl,
                                bio,
                                profileUser.getCreatedAt(),
                                postCount,
                                friendCount,
                                isFriend,
                                requestPending);
        }

        // ----------------- ACTUALIZACIONES -----------------

        @Transactional
        public void updateBio(String bio) {

                User user = authService.getCurrentUser();

                String normalized = (bio == null) ? "" : bio.trim();
                user.setBio(normalized);
        }

        @Transactional
        public void updateDisplayName(String displayName) {

                User user = authService.getCurrentUser();

                user.setDisplayName(displayName.trim());
        }

        // ----------------- AMIGOS -----------------

        // Listar amigos de un usuario
        public List<FriendDto> getFriends(String username) {

                User user = userRepository.findByUsernameIgnoreCase(username.trim())
                                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

                return friendRepository.findAllFriends(user.getId())
                                .stream()
                                .map(u -> new FriendDto(u.getId(), u.getUsername(), u.getDisplayName()))
                                .collect(Collectors.toList());
        }

        // Listar solicitudes pendientes recibidas
        public List<FriendRequestDto> getPendingRequests() {
                User currentUser = authService.getCurrentUser();
                return friendRepository.findPendingRequests(currentUser.getId())
                                .stream()
                                .map(f -> new FriendRequestDto(
                                                f.getRequester().getId(),
                                                f.getRequester().getUsername(),
                                                f.getRequester().getDisplayName(),
                                                f.getCreatedAt()))
                                .collect(Collectors.toList());
        }

}

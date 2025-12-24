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

        // Obtener mi perfil
        public UserProfileDto getMyProfile() {

                User currentUser = authService.getCurrentUser();

                long postCount = postRepository.countByUserId(currentUser.getId());
                long friendCount = friendRepository.countFriends(currentUser.getId());

                String avatarUrl = "/api/v1/avatars/" + currentUser.getAvatarKey();
                String bio = Optional.ofNullable(currentUser.getBio()).orElse("");
                String bannerColor = Optional.ofNullable(currentUser.getBannerColor())
                                .orElse("#1da1f2"); // default lindo

                return new UserProfileDto(
                                currentUser.getId(),
                                currentUser.getUsername(),
                                currentUser.getDisplayName(),
                                avatarUrl,
                                bio,
                                currentUser.getCreatedAt(),
                                postCount,
                                friendCount,
                                true, // isFriend → siempre true para uno mismo
                                false, // sentRequest → nunca aplica
                                false,
                                bannerColor); // receivedRequest → nunca aplica
        }

        // Obtener perfil público
        public UserProfileDto getUserProfile(String username) {

                User currentUser = authService.getCurrentUser();

                User profileUser = userRepository.findByUsernameIgnoreCase(username.trim())
                                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

                // Obtener la relación de amistad entre ambos usuarios (bidireccional)
                Optional<Friend> relationOpt = friendRepository.findRelationBetween(currentUser, profileUser);

                boolean isFriend = false;
                boolean sentRequest = false;
                boolean receivedRequest = false;

                if (relationOpt.isPresent()) {
                        Friend rel = relationOpt.get();
                        isFriend = rel.getStatus() == Friend.FriendStatus.ACCEPTED;
                        sentRequest = rel.getRequester().equals(currentUser)
                                        && rel.getStatus() == Friend.FriendStatus.PENDING;
                        receivedRequest = rel.getReceiver().equals(currentUser)
                                        && rel.getStatus() == Friend.FriendStatus.PENDING;
                }

                long postCount = postRepository.countByUserId(profileUser.getId());
                long friendCount = friendRepository.countFriends(profileUser.getId());

                String avatarUrl = "/api/v1/avatars/" + profileUser.getAvatarKey();
                String bio = Optional.ofNullable(profileUser.getBio()).orElse("");
                String bannerColor = Optional.ofNullable(profileUser.getBannerColor())
                                .orElse("#1da1f2"); // default lindo

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
                                sentRequest,
                                receivedRequest,
                                bannerColor);
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

        @Transactional
        public void updateBannerColor(String color) {
                User user = authService.getCurrentUser();
                user.setBannerColor(color.trim());
        }

        // ----------------- AMIGOS -----------------

        // Listar amigos de un usuario
        public List<FriendDto> getFriends(String username) {

                User user = userRepository.findByUsernameIgnoreCase(username.trim())
                                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

                return friendRepository.findAllFriends(user.getId())
                                .stream()
                                .map(u -> new FriendDto(u.getId(), u.getUsername(), u.getDisplayName(),
                                                "/api/v1/avatars/" + u.getAvatarKey()))
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
                                                "/api/v1/avatars/" + f.getRequester().getAvatarKey(),
                                                f.getCreatedAt()))
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<FriendRequestDto> getSentRequests() {
                User currentUser = authService.getCurrentUser();

                return friendRepository.findSentPendingRequests(currentUser.getId())
                                .stream()
                                .map(f -> new FriendRequestDto(
                                                f.getReceiver().getId(),
                                                f.getReceiver().getUsername(),
                                                f.getReceiver().getDisplayName(),
                                                "/api/v1/avatars/" + f.getReceiver().getAvatarKey(),
                                                f.getCreatedAt()))
                                .collect(Collectors.toList());
        }

}

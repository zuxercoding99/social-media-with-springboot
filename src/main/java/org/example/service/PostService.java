package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.PostDto;
import org.example.entity.*;
import org.example.exception.customs.httpstatus.BadRequestException;
import org.example.exception.customs.httpstatus.NotFoundException;
import org.example.repository.FriendRepository;
import org.example.repository.PostRepository;
import org.example.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepo;
    private final UserRepository userRepo;
    private final FileService fileService;
    private final FriendRepository friendRepo;
    private final StorageService storageService;
    private final AuthService authService;

    // -------------------------------------------------------
    // CREATE POST
    // -------------------------------------------------------

    @CacheEvict(value = { "feeds", "postsByUser", "posts" }, allEntries = true)
    @Transactional
    public PostDto createPost(String content, Privacy privacy, MultipartFile file) throws IOException {

        User author = authService.getCurrentUser();

        if (content == null || content.isBlank()) {
            throw new BadRequestException("El contenido no puede estar vacío");
        }

        Post post = Post.builder()
                .content(content)
                .privacy(privacy)
                .user(author)
                .build();

        Post saved = postRepo.save(post);

        if (file != null && !file.isEmpty()) {
            FileMetadata meta = fileService.uploadForPost(file, author, saved);
            saved.setFileMetadata(meta);
        }
        long count = postRepo.countComments(post.getId());
        return PostDto.from(saved, count);
    }

    // -------------------------------------------------------
    // GET POST BY ID (PROXY + CACHEADO)
    // -------------------------------------------------------

    @Transactional(readOnly = true)
    public PostDto getPostById(Long postId) {
        UUID userId = authService.getCurrentUserId();
        return getPostByIdCached(postId, userId);
    }

    @Cacheable(value = "posts", key = "#postId + '-' + #userId")
    @Transactional(readOnly = true)
    public PostDto getPostByIdCached(Long postId, UUID userId) {
        User currentUser = userRepo.getReferenceById(userId);

        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post no encontrado"));

        if (!canView(post, currentUser)) {
            throw new AccessDeniedException("No tenés permiso para ver este post");
        }

        long count = postRepo.countComments(post.getId());
        return PostDto.from(post, count);
    }

    // -------------------------------------------------------
    // FEED (PROXY + CACHEADO)
    // -------------------------------------------------------

    public Page<PostDto> getFeed(int page, int size) {
        UUID userId = authService.getCurrentUserId();
        return getFeedCached(userId, page, size);
    }

    @Cacheable(value = "feeds", key = "#userId + '-' + #page + '-' + #size")
    @Transactional(readOnly = true)
    public Page<PostDto> getFeedCached(UUID userId, int page, int size) {
        User currentUser = userRepo.getReferenceById(userId);
        Pageable pageable = PageRequest.of(page, size);
        return postRepo.findFeed(currentUser, pageable).map(post -> {
            long count = postRepo.countComments(post.getId());
            return PostDto.from(post, count);
        });
    }

    // -------------------------------------------------------
    // POSTS DEL USUARIO AUTENTICADO (NO NECESITAN CACHE)
    // -------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<PostDto> getMyPosts(int page, int size) {

        User currentUser = authService.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return postRepo.findByUser(currentUser, pageable).map(post -> {
            long count = postRepo.countComments(post.getId());
            return PostDto.from(post, count);
        });
    }

    // -------------------------------------------------------
    // POSTS DE OTRO USUARIO (PROXY + CACHEADO)
    // -------------------------------------------------------

    public Page<PostDto> getPostsByUsername(String username, int page, int size) {
        UUID viewerId = authService.getCurrentUserId();
        return getPostsByUsernameCached(username, viewerId, page, size);
    }

    @Cacheable(value = "postsByUser", key = "#username + '-' + #viewerId + '-' + #page + '-' + #size")
    @Transactional(readOnly = true)
    public Page<PostDto> getPostsByUsernameCached(
            String username, UUID viewerId, int page, int size) {

        User viewer = userRepo.getReferenceById(viewerId);

        User author = userRepo.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + username));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (author.getId().equals(viewerId)) {
            return postRepo.findByUser(author, pageable).map(post -> {
                long count = postRepo.countComments(post.getId());
                return PostDto.from(post, count);
            });
        }

        return postRepo.findVisibleByUser(author, viewer, pageable).map(post -> {
            long count = postRepo.countComments(post.getId());
            return PostDto.from(post, count);
        });
    }

    // -------------------------------------------------------
    // DELETE POST
    // -------------------------------------------------------

    @CacheEvict(value = { "feeds", "posts", "postsByUser" }, allEntries = true)
    @Transactional
    public void deletePost(Long postId) {
        User currentUser = authService.getCurrentUser();

        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post no encontrado"));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No podés eliminar un post que no es tuyo");
        }

        if (post.getFileMetadata() != null) {
            try {
                storageService.delete(
                        "posts/" + currentUser.getUsername(),
                        post.getFileMetadata().getKey());
            } catch (Exception ignored) {
            }
        }

        postRepo.delete(post);
    }

    // -------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------

    public boolean canView(Post post, User currentUser) {

        if (post.getPrivacy() == Privacy.PUBLIC)
            return true;

        if (post.getUser().getId().equals(currentUser.getId()))
            return true;

        if (post.getPrivacy() == Privacy.PRIVATE)
            return false;

        Optional<Friend> rel = friendRepo.findRelationBetween(currentUser, post.getUser());

        return rel.isPresent() &&
                rel.get().getStatus() == Friend.FriendStatus.ACCEPTED;
    }
}

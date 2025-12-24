package org.example.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.example.dto.CommentDto;
import org.example.entity.Comment;
import org.example.entity.Post;
import org.example.entity.User;
import org.example.exception.customs.httpstatus.ForbiddenException;
import org.example.exception.customs.httpstatus.NotFoundException;
import org.example.repository.CommentRepository;
import org.example.repository.PostRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepo;
    private final PostRepository postRepo;
    private final AuthService authService;
    private final PostService postService; // para validaciones canView

    // CREAR COMENTARIO
    @Transactional
    @CacheEvict(value = { "feeds", "posts", "postsByUser" }, allEntries = true)
    public CommentDto createComment(UUID postId, String content) {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("El contenido no puede estar vacío");
        }

        User currentUser = authService.getCurrentUser();
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post no encontrado"));

        // Validar privacidad
        if (!postService.canView(post, currentUser)) {
            throw new ForbiddenException("No tenés permiso para comentar este post");
        }

        Comment comment = Comment.builder()
                .content(content)
                .user(currentUser)
                .post(post)
                .build();

        post.addComment(comment); // asegura la relación bidireccional
        Comment saved = commentRepo.save(comment);

        return CommentDto.from(saved);
    }

    // OBTENER COMENTARIOS DE UN POST
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsForPost(UUID postId) {
        User currentUser = authService.getCurrentUser();

        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post no encontrado"));

        // Validar privacidad
        if (!postService.canView(post, currentUser)) {
            throw new ForbiddenException("No tenés permiso para ver los comentarios de este post");
        }

        return commentRepo.findByPostOrderByCreatedAtAsc(post)
                .stream()
                .map(CommentDto::from)
                .collect(Collectors.toList());
    }

    // ELIMINAR COMENTARIO
    @Transactional
    @CacheEvict(value = { "feeds", "posts", "postsByUser" }, allEntries = true)
    public void deleteComment(Long commentId) {
        User currentUser = authService.getCurrentUser();
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comentario no encontrado"));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("No podés eliminar un comentario que no es tuyo");
        }

        comment.getPost().removeComment(comment);
        commentRepo.delete(comment);
    }

    // CONTAR COMENTARIOS
    @Transactional(readOnly = true)
    public long countComments(UUID postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post no encontrado"));

        return commentRepo.countByPost(post);
    }
}

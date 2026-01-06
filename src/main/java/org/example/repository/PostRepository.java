package org.example.repository;

import java.util.UUID;

import org.example.entity.Post;
import org.example.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, UUID> {
    Page<Post> findByUser(User user, Pageable pageable);

    // 1) Todos los posts visibles en el feed
    @EntityGraph(attributePaths = "user")
    @Query("""
                SELECT p FROM Post p
                WHERE
                    p.privacy = org.example.entity.Privacy.PUBLIC
                    OR p.user = :currentUser
                    OR (
                        p.privacy = org.example.entity.Privacy.FRIENDS
                        AND EXISTS (
                            SELECT f FROM Friend f
                            WHERE (
                                (f.requester = p.user AND f.receiver = :currentUser)
                                OR (f.requester = :currentUser AND f.receiver = p.user)
                            )
                            AND f.status = org.example.entity.Friend.FriendStatus.ACCEPTED
                        )
                    )
                ORDER BY p.createdAt DESC
            """)
    Page<Post> findFeed(@Param("currentUser") User currentUser, Pageable pageable);

    // 2) Posts visibles de un usuario específico para otro
    @Query("""
            SELECT p FROM Post p
            WHERE p.user = :author
            AND (
                p.privacy = org.example.entity.Privacy.PUBLIC
                OR :currentUser = :author
                OR (
                    p.privacy = org.example.entity.Privacy.FRIENDS
                    AND EXISTS (
                        SELECT f FROM Friend f
                        WHERE (
                            (f.requester = p.user AND f.receiver = :currentUser)
                            OR (f.requester = :currentUser AND f.receiver = p.user)
                        )
                        AND f.status = org.example.entity.Friend.FriendStatus.ACCEPTED
                    )
                )
            )
            ORDER BY p.createdAt DESC
            """)
    Page<Post> findVisibleByUser(@Param("author") User author, @Param("currentUser") User currentUser,
            Pageable pageable);

    // Contar posts de un usuario por su id
    long countByUserId(UUID userId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    long countComments(@Param("postId") UUID postId);
}
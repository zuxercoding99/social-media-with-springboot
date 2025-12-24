package org.example.repository;

import java.util.Optional;

import org.example.entity.Post;
import org.example.entity.PostLike;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostAndUser(Post post, User user);

    Optional<PostLike> findByPostAndUser(Post post, User user);

    long countByPost(Post post);
}

package org.example.repository;

import java.util.List;

import org.example.entity.Comment;
import org.example.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostOrderByCreatedAtAsc(Post post);

    long countByPost(Post post);
}

package org.example.repository;

import java.util.List;

import org.example.entity.Friend;
import org.example.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByFriendOrderBySentAtAsc(Friend friend);

    // Último mensaje por chat
    Message findTopByFriendOrderBySentAtDesc(Friend friend);
}

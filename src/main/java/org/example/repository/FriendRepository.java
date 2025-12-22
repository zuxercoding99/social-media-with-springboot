package org.example.repository;

import org.example.entity.Friend;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRepository extends JpaRepository<Friend, Long> {

        // Buscar relacion entre dos usuarios
        @Query("""
                        SELECT f FROM Friend f
                        WHERE
                            (f.requester = :a AND f.receiver = :b)
                            OR
                            (f.requester = :b AND f.receiver = :a)
                        """)
        Optional<Friend> findRelationBetween(@Param("a") User a, @Param("b") User b);

        // // Buscar relacion entre dos usuarios por user id
        @Query("""
                            SELECT f FROM Friend f
                            WHERE
                                (f.requester.id = :id1 AND f.receiver.id = :id2)
                                OR
                                (f.requester.id = :id2 AND f.receiver.id = :id1)
                        """)
        Optional<Friend> findRelationBetweenUserIds(UUID id1, UUID id2);

        // Buscar si hay una pending request enviada por usuario A a usuario B
        @Query("""
                            SELECT f FROM Friend f
                            WHERE f.requester.id = :requesterId
                              AND f.receiver.id = :receiverId
                              AND f.status = 'PENDING'
                        """)
        Optional<Friend> findPendingRequest(
                        UUID requesterId,
                        UUID receiverId);

        @Query("""
                            SELECT f.receiver
                            FROM Friend f
                            WHERE f.requester.id = :userId
                              AND f.status = 'ACCEPTED'

                            UNION

                            SELECT f.requester
                            FROM Friend f
                            WHERE f.receiver.id = :userId
                              AND f.status = 'ACCEPTED'
                        """)
        List<User> findAllFriends(UUID userId);

        @Query("""
                        SELECT COUNT(f) FROM Friend f
                        WHERE (f.requester.id = :userId OR f.receiver.id = :userId)
                        AND f.status = 'ACCEPTED'
                        """)
        long countFriends(UUID userId);

        @Query("""
                        SELECT f FROM Friend f
                        WHERE f.receiver.id = :userId
                        AND f.status = 'PENDING'
                        """)
        List<Friend> findPendingRequests(UUID userId);

        // Buscar todas las relaciones de amigo de un usuario con cierto status
        @Query("""
                            SELECT f FROM Friend f
                            WHERE f.status = :status
                              AND (f.requester = :user OR f.receiver = :user)
                        """)
        List<Friend> findAllByFriendRelationsOfUserByStatus(
                        @Param("user") User user,
                        @Param("status") Friend.FriendStatus status);

        @Query("""
                            SELECT f FROM Friend f
                            WHERE f.requester.id = :userId
                            AND f.status = 'PENDING'
                        """)
        List<Friend> findSentPendingRequests(UUID userId);

}
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

        // Encuentra la relación en un sentido: requester → receiver
        Optional<Friend> findByRequesterAndReceiver(User requester, User receiver);

        // Alternativa
        @Query("""
                            SELECT f FROM Friend f
                            WHERE f.requester = :requester
                              AND f.receiver = :receiver
                        """)
        Optional<Friend> findDirectRelation(User requester, User receiver);

        Optional<Friend> findByRequesterIdAndReceiverId(UUID requesterId, UUID receiverId);

        // Alternativa
        @Query("""
                            SELECT f FROM Friend f
                            WHERE f.requester.id = :requesterId
                              AND f.receiver.id = :receiverId
                        """)
        Optional<Friend> findDirectRelationByIds(
                        UUID requesterId,
                        UUID receiverId);

        // Buscar si ya existe relación en cualquier sentido
        Optional<Friend> findByRequesterAndReceiverOrRequesterAndReceiver(
                        User requester1, User receiver1,
                        User requester2, User receiver2);

        // Alternativa
        @Query("""
                        SELECT f FROM Friend f
                        WHERE
                            (f.requester = :a AND f.receiver = :b)
                            OR
                            (f.requester = :b AND f.receiver = :a)
                        """)
        Optional<Friend> findRelationBetween(@Param("a") User a, @Param("b") User b);

        // Alternativa con ids
        @Query("""
                            SELECT f FROM Friend f
                            WHERE
                                (f.requester.id = :id1 AND f.receiver.id = :id2)
                                OR
                                (f.requester.id = :id2 AND f.receiver.id = :id1)
                        """)
        Optional<Friend> findRelationBetweenUserIds(UUID id1, UUID id2);

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
        List<Friend> findAllByUserAndStatus(
                        @Param("user") User user,
                        @Param("status") Friend.FriendStatus status);

}
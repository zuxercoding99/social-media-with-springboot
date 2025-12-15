package org.example.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friends", uniqueConstraints = @UniqueConstraint(columnNames = { "requester_id",
        "receiver_id" }), indexes = {
                @Index(name = "idx_friends_requester", columnList = "requester_id"),
                @Index(name = "idx_friends_receiver", columnList = "receiver_id"),
                @Index(name = "idx_friends_status", columnList = "status")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friend {

    @Id
    @GeneratedValue
    private Long id;

    // Usuario que envía la solicitud
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // Usuario que recibe la solicitud
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status;

    public enum FriendStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        BLOCKED
    }
}
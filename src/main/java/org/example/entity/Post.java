package org.example.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "TEXT", length = 300)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Privacy privacy; // PUBLIC / PRIVATE / FRIENDS

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, optional = true)
    private FileMetadata fileMetadata;

    public void setFileMetadata(FileMetadata fileMetadata) {
        if (this.fileMetadata != null) {
            this.fileMetadata.setPost(null); // deja huérfana la fileMetadata anterior
        }

        this.fileMetadata = fileMetadata;

        if (fileMetadata != null && fileMetadata.getPost() != this) {
            fileMetadata.setPost(this); // sincroniza lado inverso
        }
    }

    public void removeFileMetadata() {
        if (this.fileMetadata != null) {
            this.fileMetadata.setPost(null);
            this.fileMetadata = null;
        }
    }

}
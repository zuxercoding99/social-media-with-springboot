package org.example.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "files", indexes = {
        @Index(name = "idx_files_user_id", columnList = "user_id"),
        @Index(name = "idx_files_created_at", columnList = "createdAt")
})
@Getter
@Setter
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_key", nullable = false, unique = true)
    private String key; // nombre/clave única en el storage

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String contentType; // image/png, image/jpeg, etc.

    @Column(nullable = false)
    private long size; // tamaño en bytes

    @Column(nullable = false)
    private String url; // URL pública o relativa

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private Post post;

}
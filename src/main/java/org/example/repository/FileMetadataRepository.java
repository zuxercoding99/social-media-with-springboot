package org.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.example.entity.FileMetadata;
import org.example.entity.User;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByOwner(User owner);

    Optional<FileMetadata> findByKey(String filename);

    @Query("SELECT f.contentType FROM FileMetadata f WHERE f.key = :key")
    Optional<String> findContentTypeByKey(@Param("key") String key);
}

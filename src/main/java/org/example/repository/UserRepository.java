package org.example.repository;

import java.util.Optional;
import java.util.UUID;

import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByProviderId(String providerId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}
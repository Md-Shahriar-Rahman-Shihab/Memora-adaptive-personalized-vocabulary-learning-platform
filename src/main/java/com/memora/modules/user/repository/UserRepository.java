package com.memora.modules.user.repository;

import com.memora.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for User entity persistence and querying.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) AND u.id != :currentUserId ORDER BY u.name ASC")
    java.util.List<User> searchByNameExcludingUser(@org.springframework.data.repository.query.Param("query") String query,
                                                   @org.springframework.data.repository.query.Param("currentUserId") Long currentUserId);
}

package com.cloudlens.api.repository;

import com.cloudlens.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.role) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY CASE WHEN u.role = 'ADMIN' THEN 0 ELSE 1 END, u.username")
    List<User> searchUsers(@Param("search") String search);
}

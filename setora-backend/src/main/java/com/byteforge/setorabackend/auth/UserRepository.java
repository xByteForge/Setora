package com.byteforge.setorabackend.auth;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    @Query("""
    SELECT u
    FROM User u
    WHERE u.isDeleted = false
    ORDER BY u.id ASC    
    """)
    List<User> findAllActive();

    @Query("""
    SELECT COUNT(u)
    FROM User u
    WHERE u.role = :role AND u.isDeleted = false
    """)
    long countActiveByRole(@Param("role") UserRole role);
}

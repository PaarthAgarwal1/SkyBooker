package com.skybooker.AuthService.repository;

import com.skybooker.AuthService.entity.Role;
import com.skybooker.AuthService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(UUID userId);

    boolean existsByEmail(String email);

    List<User> findAllByRole(Role role);

    Optional<User> findByPhone(String phone);

    Optional<User> findByPassportNumber(String passportNumber);

    void deleteByUserId(UUID userId);
}

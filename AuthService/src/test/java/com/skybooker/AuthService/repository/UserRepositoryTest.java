package com.skybooker.AuthService.repository;

import com.skybooker.AuthService.entity.ApprovalStatus;
import com.skybooker.AuthService.entity.Role;
import com.skybooker.AuthService.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindByEmail() {
        User user = User.builder()
                .fullName("Test User")
                .email("user@test.com")
                .passwordHash("encoded")
                .role(Role.PASSENGER)
                .approvalStatus(ApprovalStatus.APPROVED)
                .isActive(true)
                .build();

        userRepository.save(user);

        assertTrue(userRepository.findByEmail("user@test.com").isPresent());
        assertEquals("Test User", userRepository.findByEmail("user@test.com").get().getFullName());
    }
}

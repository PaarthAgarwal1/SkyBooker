package com.skybooker.AuthService.service;

import com.skybooker.AuthService.client.AirlineClient;
import com.skybooker.AuthService.dto.request.RegisterDTO;
import com.skybooker.AuthService.repository.UserRepository;
import com.skybooker.AuthService.security.JwtUtil;
import com.skybooker.AuthService.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AirlineClient airlineClient;
    @Mock private UserRepository userRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ModelMapper modelMapper;
    @Mock private RegistrationKeyValidator keyValidator;
    @Mock private FileUploadService fileUploadService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerCreatesUser() {
        RegisterDTO request = new RegisterDTO();
        request.setFullName("Test User");
        request.setEmail("user@test.com");
        request.setPassword("password");

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        assertEquals("Register Successfully", authService.register(request).getMessage());
        verify(userRepository).save(any());
    }
}

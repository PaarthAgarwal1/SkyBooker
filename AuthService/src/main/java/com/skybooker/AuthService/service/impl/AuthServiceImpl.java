package com.skybooker.AuthService.service.impl;

import com.skybooker.AuthService.dto.request.ChangePasswordDTO;
import com.skybooker.AuthService.dto.request.LoginDTO;
import com.skybooker.AuthService.dto.request.RegisterDTO;
import com.skybooker.AuthService.dto.request.UpdateProfileDTO;
import com.skybooker.AuthService.dto.response.AuthResponse;
import com.skybooker.AuthService.dto.response.UserResponse;
import com.skybooker.AuthService.entity.Role;
import com.skybooker.AuthService.entity.User;
import com.skybooker.AuthService.exception.EmailAlreadyExistsException;
import com.skybooker.AuthService.exception.InvalidCredentialsException;
import com.skybooker.AuthService.exception.UserNotFoundException;
import com.skybooker.AuthService.repository.UserRepository;
import com.skybooker.AuthService.security.JwtUtil;
import com.skybooker.AuthService.service.AuthService;
import com.skybooker.AuthService.service.RegistrationKeyValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final RegistrationKeyValidator keyValidator;

    @Override
    public AuthResponse register(RegisterDTO request) {
        if(userRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistsException("Email already exists");
        }

        Role role = keyValidator.resolveRole(request.getRegistrationKey());


        User user=User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();
        userRepository.save(user);
        AuthResponse authResponse=new AuthResponse();
        authResponse.setMessage("Register Successfully");
        return authResponse;
    }

    @Override
    public AuthResponse login(LoginDTO request) {

        Optional<User> user= Optional.of(userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UserNotFoundException("Invalid credentials")));

        if(!passwordEncoder.matches(request.getPassword(),user.get().getPasswordHash())){
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token=jwtUtil.generateToken(user.get().getEmail(),user.get().getRole().name());

        AuthResponse authResponse=new AuthResponse();
        authResponse.setMessage("Login Successfully");
        authResponse.setJwtToken(token);
        return authResponse;
    }

    @Override
    public UserResponse getProfile(String email) {
        User userFromDb=userRepository.findByEmail(email).orElseThrow(()->new UserNotFoundException("User not found"));
        return modelMapper.map(userFromDb,UserResponse.class);
    }

    @Override
    public UserResponse updateProfile(String email, UpdateProfileDTO request) {
        User userFromDb=userRepository.findByEmail(email).orElseThrow(()->new UserNotFoundException("User not found"));
        userFromDb.setNationality(request.getNationality());
        userFromDb.setPhone(request.getPhone());
        userFromDb.setFullName(request.getFullName());
        userFromDb.setPassportNumber(request.getPassportNumber());
        User updatedUser=userRepository.save(userFromDb);
        return modelMapper.map(updatedUser,UserResponse.class);
    }

    @Override
    public void changePassword(String email, ChangePasswordDTO request) {
        User userFromDb=userRepository.findByEmail(email).orElseThrow(()->new UserNotFoundException("User not found"));
        if(!passwordEncoder.matches(request.getOldPassword(),userFromDb.getPasswordHash())){
           throw new InvalidCredentialsException("Invalid Old Password");
        }
        userFromDb.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(userFromDb);
    }

    @Override
    public void deactivateAccount(String email) {
        User user=userRepository.findByEmail(email).orElseThrow(()->new UserNotFoundException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users= userRepository.findAll();

        return users.stream()
                .map(user->modelMapper.map(user,UserResponse.class))
                .collect(Collectors.toList());
    }
}

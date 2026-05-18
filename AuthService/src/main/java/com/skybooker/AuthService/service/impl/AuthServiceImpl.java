package com.skybooker.AuthService.service.impl;

import com.skybooker.AuthService.client.AirlineClient;
import com.skybooker.AuthService.dto.request.ChangePasswordDTO;
import com.skybooker.AuthService.dto.request.LoginDTO;
import com.skybooker.AuthService.dto.request.RegisterDTO;
import com.skybooker.AuthService.dto.request.UpdateProfileDTO;
import com.skybooker.AuthService.dto.response.AirlineResponse;
import com.skybooker.AuthService.dto.response.AuthResponse;
import com.skybooker.AuthService.dto.response.UserResponse;
import com.skybooker.AuthService.entity.ApprovalStatus;
import com.skybooker.AuthService.entity.Role;
import com.skybooker.AuthService.entity.User;
import com.skybooker.AuthService.exception.EmailAlreadyExistsException;
import com.skybooker.AuthService.exception.InvalidCredentialsException;
import com.skybooker.AuthService.exception.UserNotFoundException;
import com.skybooker.AuthService.repository.UserRepository;
import com.skybooker.AuthService.security.JwtUtil;
import com.skybooker.AuthService.service.AuthService;
import com.skybooker.AuthService.service.FileUploadService;
import com.skybooker.AuthService.service.RegistrationKeyValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String USER_NOT_FOUND = "User not found";

    private final AirlineClient airlineClient;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final RegistrationKeyValidator keyValidator;
    private final FileUploadService fileUploadService;

    @Override
    public AuthResponse register(RegisterDTO request) {

        log.info("Register request received for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed. Email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException("Email already exists");
        }

        Role role;

        if (request.getRegistrationKey() == null || request.getRegistrationKey().isBlank()) {
            role = Role.PASSENGER;
        } else {
            role = keyValidator.resolveRole(request.getRegistrationKey());
        }

        if (role == null) {
            role = Role.PASSENGER;
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        if (role == Role.AIRLINE_STAFF) {
            user.setApprovalStatus(ApprovalStatus.PENDING);
        } else {
            user.setApprovalStatus(ApprovalStatus.APPROVED);
        }

        userRepository.save(user);

        log.info("User registered successfully with role: {}", role);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("Register Successfully");

        return authResponse;
    }

    @Override
    public AuthResponse login(LoginDTO request) {

        log.info("Login attempt for email: {}", request.getEmail());

        Optional<User> user = Optional.of(
                userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> {
                            log.warn("User not found for email in login: {}", request.getEmail());
                            return new UserNotFoundException("Invalid credentials");
                        })
        );

        if (!user.get().isActive()) {
            log.warn("Deactivated account login attempt: {}", request.getEmail());
            throw new RuntimeException("Account is deactivated");
        }

        if (user.get().getRole() == Role.AIRLINE_STAFF &&
                user.get().getApprovalStatus() != ApprovalStatus.APPROVED) {

            log.warn("Airline staff account not approved: {}", request.getEmail());
            throw new RuntimeException("Your account is not approved by admin yet");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.get().getPasswordHash())) {
            log.warn("Invalid password attempt for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(
                user.get().getEmail(),
                user.get().getRole().name(),
                user.get().getAirlineId()
        );

        log.info("Login successful for email: {}", request.getEmail());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("Login Successfully");
        authResponse.setJwtToken(token);

        return authResponse;
    }

    @Override
    public UserResponse getProfile(String email) {

        log.info("Fetching profile for email: {}", email);

        User userFromDb = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for email in getProfile : {}", email);
                    return new UserNotFoundException(USER_NOT_FOUND);
                });

        return modelMapper.map(userFromDb, UserResponse.class);
    }

    @Override
    public UserResponse updateProfile(String email, UpdateProfileDTO request) {

        log.info("Updating profile for email: {}", email);
        log.debug("Profile update request: {}", request);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for email in updateProfile: {}", email);
                    return new UserNotFoundException(USER_NOT_FOUND);
                });

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
        }

        if (request.getPassportNumber() != null && !request.getPassportNumber().isBlank()) {
            user.setPassportNumber(request.getPassportNumber());
        }

        if (request.getNationality() != null && !request.getNationality().isBlank()) {
            user.setNationality(request.getNationality());
        }

        User updated = userRepository.save(user);

        log.info("Profile updated successfully for email: {}", email);
        log.info("User Role: {}", user.getRole());
        log.info("Airline ID: {}", user.getAirlineId());
        log.info("Airline Name: {}", user.getAirlineName());
        log.info("Approval Status: {}", user.getApprovalStatus());

        return mapToResponse(updated);
    }

    @Override
    public UserResponse updateProfileImage(String email, MultipartFile file) {

        log.info("Updating profile image for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for email in updateProfileImage: {}", email);
                    return new UserNotFoundException(USER_NOT_FOUND);
                });

        if (file.isEmpty()) {
            log.warn("Uploaded file is empty for email: {}", email);
            throw new RuntimeException("File is empty");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                (!contentType.equals("image/jpeg")
                        && !contentType.equals("image/png")
                        && !contentType.equals("image/webp"))) {

            log.warn("Invalid file type uploaded by email: {}", email);
            throw new RuntimeException("Only JPG, PNG, WEBP allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            log.warn("File size exceeds limit for email: {}", email);
            throw new RuntimeException("File size exceeds 5MB");
        }

        String imageUrl = fileUploadService.uploadImage(file);

        user.setProfileImageUrl(imageUrl);

        log.info("Profile image updated successfully for email: {}", email);

        return mapToResponse(userRepository.save(user));
    }

    @Override
    public void changePassword(String email, ChangePasswordDTO request) {

        log.info("Password change request for email: {}", email);

        User userFromDb = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for email in changePassword: {}", email);
                    return new UserNotFoundException(USER_NOT_FOUND);
                });

        if (!passwordEncoder.matches(request.getOldPassword(), userFromDb.getPasswordHash())) {

            log.warn("Invalid old password provided for email: {}", email);
            throw new InvalidCredentialsException("Invalid Old Password");
        }

        userFromDb.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(userFromDb);

        log.info("Password changed successfully for email: {}", email);
    }

    @Override
    public void deactivateAccount(String email) {

        log.info("Deactivating account for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for email: {}", email);
                    return new UserNotFoundException(USER_NOT_FOUND);
                });

        user.setActive(false);
        user.setDeactivatedByAdmin(false);

        userRepository.save(user);

        log.info("Account deactivated successfully for email: {}", email);
    }

    @Override
    public List<UserResponse> getAllUsers() {

        log.info("Fetching all users");

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> modelMapper.map(user, UserResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(UUID userId) {

        log.info("Fetching user by ID: {}", userId);

        User userFromDb = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID in getUserById: {}", userId);
                    return new UserNotFoundException(USER_NOT_FOUND);
                });

        return modelMapper.map(userFromDb, UserResponse.class);
    }

    @Override
    public void adminActivateUser(UUID userId) {

        log.info("Activating user with ID: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID in adminActivateUser: {}", userId);
                    return new UserNotFoundException(USER_NOT_FOUND);
                });

        user.setActive(true);
        user.setDeactivatedByAdmin(false);

        userRepository.save(user);

        log.info("User activated successfully with ID: {}", userId);
    }

    @Override
    public void adminDeactivateUser(UUID userId) {

        log.info("Admin deactivating user with ID: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID in adminDeactivateUser: {}", userId);
                    return new UserNotFoundException(USER_NOT_FOUND);
                });

        if (user.getRole() == Role.ADMIN) {
            log.warn("Attempt to deactivate admin account. User ID: {}", userId);
            throw new RuntimeException("Admins cannot be modified");
        }

        user.setActive(false);
        user.setDeactivatedByAdmin(true);

        userRepository.save(user);

        log.info("User deactivated by admin successfully. User ID: {}", userId);
    }

    @Override
    public void deleteUser(UUID userId) {

        log.info("Deleting user with ID: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID in deleteUser: {}", userId);
                    return new UserNotFoundException(USER_NOT_FOUND);
                });

        userRepository.delete(user);

        log.info("User deleted successfully with ID: {}", userId);
    }

    @Override
    public void assignAirline(UUID userId, UUID airlineId) {

        log.info("Assigning airline {} to user {}", airlineId, userId);

        AirlineResponse res;

        try {

            log.info("Calling airline service for airlineId: {}", airlineId);

            res = airlineClient.getAirlineById(airlineId);

            log.info("Received airline response: {}", res);

        } catch (Exception e) {

            log.error("Feign client error while fetching airline", e);

            throw new RuntimeException("Feign failed: " + e.getMessage());
        }

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UserNotFoundException(USER_NOT_FOUND);
                });

        if (user.getRole() != Role.AIRLINE_STAFF) {

            log.warn("User is not airline staff. User ID: {}", userId);

            throw new RuntimeException("User is not airline staff");
        }

        user.setAirlineId(airlineId);
        user.setAirlineName(res.getAirlineName());
        user.setApprovalStatus(ApprovalStatus.APPROVED);

        userRepository.save(user);

        log.info("Airline assigned successfully. UserId: {}, AirlineId: {}",
                userId, airlineId);
    }

    private UserResponse mapToResponse(User user) {

        UserResponse res = new UserResponse();

        res.setUserId(user.getUserId());
        res.setFullName(user.getFullName());
        res.setEmail(user.getEmail());
        res.setPhone(user.getPhone());
        res.setNationality(user.getNationality());
        res.setPassportNumber(user.getPassportNumber());
        res.setProfileImageUrl(user.getProfileImageUrl());
        res.setRole(user.getRole());
        res.setCreatedAt(user.getCreatedAt());

        res.setAirlineId(user.getAirlineId());
        res.setAirlineName(user.getAirlineName());

        res.setApprovalStatus(
                user.getApprovalStatus() != null
                        ? user.getApprovalStatus().name()
                        : null
        );

        log.debug("Mapped user response for email: {}", user.getEmail());

        return res;
    }
}
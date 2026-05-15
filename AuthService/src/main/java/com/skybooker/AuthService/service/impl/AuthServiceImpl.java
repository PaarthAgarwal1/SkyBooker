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
public class AuthServiceImpl implements AuthService {

    private final AirlineClient airlineClient;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final RegistrationKeyValidator keyValidator;
    private final FileUploadService fileUploadService;

    @Override
    public AuthResponse register(RegisterDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        Role role;

        if (request.getRegistrationKey() == null || request.getRegistrationKey().isBlank()) {
            role = Role.PASSENGER; // ✅ fallback
        } else {
            role = keyValidator.resolveRole(request.getRegistrationKey());
        }

        if (role == null) {
            role = Role.PASSENGER; // ✅ double safety
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        if(role == Role.AIRLINE_STAFF){
            user.setApprovalStatus(ApprovalStatus.PENDING);
        } else {
            user.setApprovalStatus(ApprovalStatus.APPROVED);
        }

        userRepository.save(user);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("Register Successfully");

        return authResponse;
    }

    @Override
    public AuthResponse login(LoginDTO request) {

        Optional<User> user= Optional.of(userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UserNotFoundException("Invalid credentials")));

        if (!user.get().isActive()) {
            throw new RuntimeException("Account is deactivated");
        }
        if(user.get().getRole() == Role.AIRLINE_STAFF &&
                user.get().getApprovalStatus() != ApprovalStatus.APPROVED){
            throw new RuntimeException("Your account is not approved by admin yet");
        }

        if(!passwordEncoder.matches(request.getPassword(),user.get().getPasswordHash())){
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(
                user.get().getEmail(),
                user.get().getRole().name(),
                user.get().getAirlineId()
        );
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

        System.out.println("profile update data "+request);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

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
        System.out.println("ROLE = " + user.getRole());
        System.out.println("AIRLINE ID = " + user.getAirlineId());
        System.out.println("AIRLINE NAME = " + user.getAirlineName());
        System.out.println("APPROVAL = " + user.getApprovalStatus());
        return mapToResponse(updated);
    }

    @Override
    public UserResponse updateProfileImage(String email, MultipartFile file) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                (!contentType.equals("image/jpeg")
                        && !contentType.equals("image/png")
                        && !contentType.equals("image/webp"))) {

            throw new RuntimeException("Only JPG, PNG, WEBP allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds 5MB");
        }

        String imageUrl = fileUploadService.uploadImage(file);

        user.setProfileImageUrl(imageUrl);

        return mapToResponse(userRepository.save(user));
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setActive(false);
        user.setDeactivatedByAdmin(false);

        userRepository.save(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users= userRepository.findAll();

        return users.stream()
                .map(user->modelMapper.map(user,UserResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        User userFromDb=userRepository.findByUserId(userId).orElseThrow(()->new UserNotFoundException("User not found"));
        return modelMapper.map(userFromDb,UserResponse.class);
    }

    @Override
    public void adminActivateUser(UUID userId) {
        System.out.println(userId);
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setActive(true);
        user.setDeactivatedByAdmin(false);

        userRepository.save(user);
    }

    @Override
    public void adminDeactivateUser(UUID userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Admins cannot be modified");
        }
        user.setActive(false);
        user.setDeactivatedByAdmin(true); // ⭐ important

        userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userRepository.delete(user);
    }

    @Override
    public void assignAirline(UUID userId, UUID airlineId){

        // ✅ Validate airline exists
        AirlineResponse res;
        try {
            System.out.println("Calling airline service...");
            res = airlineClient.getAirlineById(airlineId);
            System.out.println("Response: " + res);
        } catch (Exception e) {
            e.printStackTrace(); // 🔥 THIS WILL SHOW REAL ERROR
            throw new RuntimeException("Feign failed: " + e.getMessage());
        }

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if(user.getRole() != Role.AIRLINE_STAFF){
            throw new RuntimeException("User is not airline staff");
        }

        user.setAirlineId(airlineId);
        user.setAirlineName(res.getAirlineName());
        user.setApprovalStatus(ApprovalStatus.APPROVED);

        userRepository.save(user);
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

        // SAFE STAFF FIELDS
        res.setAirlineId(user.getAirlineId());
        res.setAirlineName(user.getAirlineName());

        res.setApprovalStatus(
                user.getApprovalStatus() != null
                        ? user.getApprovalStatus().name()
                        : null
        );
        System.out.println("ROLE = " + user.getRole());
        System.out.println("AIRLINE ID = " + user.getAirlineId());
        System.out.println("AIRLINE NAME = " + user.getAirlineName());
        System.out.println("APPROVAL = " + user.getApprovalStatus());
        return res;
    }


}

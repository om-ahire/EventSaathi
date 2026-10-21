package com.eventsaathi.service;

import com.eventsaathi.dto.ApiDtos.AuthResponse;
import com.eventsaathi.dto.ApiDtos.LoginRequest;
import com.eventsaathi.dto.ApiDtos.RegisterRequest;
import com.eventsaathi.dto.ApiDtos.UserResponse;
import com.eventsaathi.entity.Role;
import com.eventsaathi.entity.User;
import com.eventsaathi.entity.enums.RoleName;
import com.eventsaathi.exception.BusinessException;
import com.eventsaathi.repository.RoleRepository;
import com.eventsaathi.repository.UserRepository;
import com.eventsaathi.security.JwtService;
import com.eventsaathi.security.UserPrincipal;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("An account with this email already exists");
        }
        Role role = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("Customer role has not been initialized"));
        User user = new User(request.name().trim(), email,
                passwordEncoder.encode(request.password()), trimToNull(request.phone()));
        user.addRole(role);
        userRepository.save(user);
        return authResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return authResponse(user);
    }

    public UserResponse currentUser(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadCredentialsException("User account is no longer available"));
        return userResponse(user);
    }

    private AuthResponse authResponse(User user) {
        return new AuthResponse(jwtService.createToken(user), "Bearer",
                jwtService.getExpirationSeconds(), userResponse(user));
    }

    private UserResponse userResponse(User user) {
        Set<String> roles = user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet());
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(), roles);
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

package com.memora.modules.user.service;

import com.memora.common.exception.EmailAlreadyExistsException;
import com.memora.common.exception.InvalidCredentialsException;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.dto.AuthResponse;
import com.memora.modules.user.dto.LoginRequest;
import com.memora.modules.user.dto.RegistrationRequest;
import com.memora.modules.user.dto.UserResponse;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.security.jwt.JwtService;
import com.memora.security.user.UserPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete implementation of {@link AuthService}.
 * Handles registration validation, password hashing, and JWT token issuance.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getName().trim(),
                request.getEmail().toLowerCase().trim(),
                hashedPassword,
                VocabularyLevel.A1,
                Role.LEARNER
        );

        User savedUser = userRepository.save(user);
        UserPrincipal principal = UserPrincipal.create(savedUser);
        String token = jwtService.generateToken(principal);

        return new AuthResponse(token, UserResponse.fromEntity(savedUser));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        UserPrincipal principal = UserPrincipal.create(user);
        String token = jwtService.generateToken(principal);

        return new AuthResponse(token, UserResponse.fromEntity(user));
    }
}

package com.memora.modules.user.service;

import com.memora.common.exception.UserNotFoundException;
import com.memora.modules.user.dto.UserResponse;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete implementation of {@link UserService}.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile(String email) {
        User user = getUserByEmail(email);
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        String normalizedEmail = email != null ? email.toLowerCase().trim() : "";
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}

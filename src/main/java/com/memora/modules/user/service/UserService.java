package com.memora.modules.user.service;

import com.memora.modules.user.dto.UserResponse;
import com.memora.modules.user.entity.User;

/**
 * Service interface for querying and managing learner profile information.
 */
public interface UserService {

    UserResponse getCurrentUserProfile(String email);

    User getUserByEmail(String email);
}

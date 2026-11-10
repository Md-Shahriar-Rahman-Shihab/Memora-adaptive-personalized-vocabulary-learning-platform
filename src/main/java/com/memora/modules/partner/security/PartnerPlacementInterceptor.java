package com.memora.modules.partner.security;

import com.memora.modules.assessment.repository.AssessmentRepository;
import com.memora.modules.learningpath.repository.LearningPathRepository;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Centralized authorization interceptor enforcing CEFR Placement Test completion
 * before allowing access to any learning partner endpoints (/api/v1/partners/**).
 */
@Component
public class PartnerPlacementInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(PartnerPlacementInterceptor.class);

    private final UserRepository userRepository;
    private final AssessmentRepository assessmentRepository;
    private final LearningPathRepository learningPathRepository;

    public PartnerPlacementInterceptor(
            UserRepository userRepository,
            AssessmentRepository assessmentRepository,
            LearningPathRepository learningPathRepository) {
        this.userRepository = userRepository;
        this.assessmentRepository = assessmentRepository;
        this.learningPathRepository = learningPathRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            // Unauthenticated requests are delegated to Spring Security (returns 401)
            return true;
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return true;
        }

        // Administrators bypass placement requirement
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // Verify placement assessment completion
        boolean hasCompletedAssessment = assessmentRepository.hasCompletedAssessment(user.getId());
        boolean hasLearningPath = learningPathRepository.findActivePathByUserId(user.getId()).isPresent()
                || !learningPathRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).isEmpty();

        if (!hasCompletedAssessment && !hasLearningPath) {
            log.warn("Access to Partner endpoint '{}' denied for user '{}': Placement test not completed",
                    request.getRequestURI(), email);
            throw new AccessDeniedException("Placement assessment must be completed before accessing the Partner section");
        }

        return true;
    }
}


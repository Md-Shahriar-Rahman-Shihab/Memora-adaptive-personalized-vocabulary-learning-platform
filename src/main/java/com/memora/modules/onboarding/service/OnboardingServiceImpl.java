package com.memora.modules.onboarding.service;

import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.assessment.domain.AssessmentStatus;
import com.memora.modules.assessment.entity.Assessment;
import com.memora.modules.assessment.repository.AssessmentRepository;
import com.memora.modules.learningpath.entity.LearningPath;
import com.memora.modules.learningpath.repository.LearningPathRepository;
import com.memora.modules.onboarding.domain.OnboardingState;
import com.memora.modules.onboarding.dto.OnboardingStateResponse;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link OnboardingService}.
 * Strictly inspects real domain data to derive the learner's onboarding progression.
 */
@Service
@Transactional(readOnly = true)
public class OnboardingServiceImpl implements OnboardingService {

    private static final Logger log = LoggerFactory.getLogger(OnboardingServiceImpl.class);

    private final UserRepository userRepository;
    private final AssessmentRepository assessmentRepository;
    private final LearningPathRepository learningPathRepository;

    public OnboardingServiceImpl(UserRepository userRepository,
                                 AssessmentRepository assessmentRepository,
                                 LearningPathRepository learningPathRepository) {
        this.userRepository = userRepository;
        this.assessmentRepository = assessmentRepository;
        this.learningPathRepository = learningPathRepository;
    }

    @Override
    public OnboardingStateResponse getOnboardingState(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Long userId = user.getId();

        // 1. Priority 1: Check for an active (IN_PROGRESS) assessment session
        Optional<Assessment> activeAssessment = assessmentRepository.findActiveAssessment(userId);
        if (activeAssessment.isPresent()) {
            log.debug("Learner {} has assessment in progress with ID: {}", userEmail, activeAssessment.get().getId());
            return new OnboardingStateResponse(
                    OnboardingState.ASSESSMENT_IN_PROGRESS,
                    activeAssessment.get().getId(),
                    null
            );
        }

        // 2. Priority 2: Check if an active or historical learning path exists
        Optional<LearningPath> activePath = learningPathRepository.findActivePathByUserId(userId);
        if (activePath.isPresent()) {
            log.debug("Learner {} has active learning path with ID: {}", userEmail, activePath.get().getId());
            return new OnboardingStateResponse(
                    OnboardingState.LEARNING_ACTIVE,
                    null,
                    activePath.get().getId()
            );
        }

        List<LearningPath> historicalPaths = learningPathRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (!historicalPaths.isEmpty()) {
            LearningPath latestPath = historicalPaths.get(0);
            log.debug("Learner {} has completed/historical learning path with ID: {}", userEmail, latestPath.getId());
            return new OnboardingStateResponse(
                    OnboardingState.LEARNING_ACTIVE,
                    null,
                    latestPath.getId()
            );
        }

        // 3. Priority 3: Check for completed assessment needing a learning path
        List<Assessment> assessments = assessmentRepository.findByUserIdOrderByStartedAtDesc(userId);
        Optional<Assessment> latestCompleted = assessments.stream()
                .filter(a -> a.getStatus() == AssessmentStatus.COMPLETED)
                .findFirst();

        if (latestCompleted.isPresent()) {
            log.debug("Learner {} has completed assessment {} but no learning path", userEmail, latestCompleted.get().getId());
            return new OnboardingStateResponse(
                    OnboardingState.LEARNING_PATH_REQUIRED,
                    latestCompleted.get().getId(),
                    null
            );
        }

        // 4. Priority 4: Brand-new learner with no assessment history
        log.debug("Learner {} has no assessment records; onboarding required", userEmail);
        return new OnboardingStateResponse(
                OnboardingState.ONBOARDING_REQUIRED,
                null,
                null
        );
    }
}

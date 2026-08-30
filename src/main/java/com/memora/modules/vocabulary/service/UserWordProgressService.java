package com.memora.modules.vocabulary.service;

import com.memora.modules.vocabulary.dto.UserWordProgressResponse;

import java.util.List;

/**
 * Service interface for managing and querying user-level word performance and review schedules.
 */
public interface UserWordProgressService {

    List<UserWordProgressResponse> getProgressForUser(String email);

    UserWordProgressResponse getProgressForWord(String email, Long wordId);

    UserWordProgressResponse initializeProgress(String email, Long wordId);

    List<UserWordProgressResponse> getWeakWords(String email);

    List<UserWordProgressResponse> getDueReviews(String email);
}

package com.memora.modules.ai.service;

import com.memora.modules.ai.dto.*;

/**
 * Service contract for AI-assisted and adaptive vocabulary learning explanations,
 * contextual examples, mnemonic retention tips, and usage guidance.
 */
public interface AIExplanationService {

    /**
     * Generates a CEFR-adapted explanation for a vocabulary word, factoring in the learner's
     * current proficiency and memory retention status.
     *
     * @param userEmail Email of the authenticated user
     * @param request {@link AiExplanationRequest}
     * @return {@link AiExplanationResponse}
     */
    AiExplanationResponse explainWord(String userEmail, AiExplanationRequest request);

    /**
     * Generates a natural example sentence tailored to the learner's CEFR level.
     *
     * @param userEmail Email of the authenticated user
     * @param request {@link AiExampleRequest}
     * @return {@link AiExampleResponse}
     */
    AiExampleResponse generateExample(String userEmail, AiExampleRequest request);

    /**
     * Generates a memorable mnemonic, phonological association, or retention tip to overcome forgetting.
     *
     * @param userEmail Email of the authenticated user
     * @param request {@link AiMemoryTipRequest}
     * @return {@link AiMemoryTipResponse}
     */
    AiMemoryTipResponse generateMemoryTip(String userEmail, AiMemoryTipRequest request);

    /**
     * Explains practical contextual usage, formal/informal register, and frequent collocations.
     *
     * @param userEmail Email of the authenticated user
     * @param request {@link AiUsageRequest}
     * @return {@link AiUsageResponse}
     */
    AiUsageResponse explainUsage(String userEmail, AiUsageRequest request);

    /**
     * Generates relevant synonyms, antonyms, and word family grammatical forms (noun, verb, adjective, adverb).
     *
     * @param userEmail Email of the active user (or "guest")
     * @param request {@link AiWordRelationsRequest}
     * @return {@link AiWordRelationsResponse}
     */
    AiWordRelationsResponse getWordRelations(String userEmail, AiWordRelationsRequest request);
}

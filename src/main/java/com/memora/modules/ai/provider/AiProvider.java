package com.memora.modules.ai.provider;

/**
 * Service contract for low-level AI text generation providers.
 * Decouples educational vocabulary services from specific LLM vendors.
 */
public interface AiProvider {

    /**
     * Generates a structured response capturing the generated text, provider, model name, and fallback status.
     *
     * @param prompt The formatted contextual prompt
     * @return Result containing generated text and provider metadata
     */
    default AiGenerationResult generate(String prompt) {
        String text = generateContent(prompt);
        return new AiGenerationResult(text, getProviderName(), null, "fallback".equalsIgnoreCase(getProviderName()));
    }

    /**
     * Generates a textual response from the underlying provider for the given prompt.
     *
     * @param prompt The formatted contextual prompt
     * @return Generated text content
     */
    String generateContent(String prompt);

    /**
     * Returns the identifying provider name (e.g., "gemini", "fallback").
     *
     * @return Provider identifier string
     */
    String getProviderName();

    /**
     * Indicates whether this provider is currently available and ready to accept requests.
     *
     * @return True if available, false otherwise
     */
    boolean isAvailable();
}

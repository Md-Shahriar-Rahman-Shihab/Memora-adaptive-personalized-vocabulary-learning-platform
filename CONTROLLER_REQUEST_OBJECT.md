# Memora — Controller Layer & Request Object Compliance Report

> **Course / Lab**: Advanced Object-Oriented Programming Laboratory  
> **Platform**: Memora (Adaptive Personalized Vocabulary Learning Platform)  
> **Subject**: Complete Controller Layer Audit & Request Object Compliance Verification  
> **Status**: Verified & Fully Compliant (0 Test Failures, 0 TypeScript Errors, 0 Build Errors)  

---

## 1. Purpose

The primary objective of this audit was to systematically inspect every `@RestController` across the backend platform to ensure that:
1. **Structured Client Inputs**: All client-submitted input payloads are encapsulated within strongly typed, validated **Request Objects / Request DTOs** (`@Valid @RequestBody`) rather than unstructured primitive parameters.
2. **REST Semantics Integrity**: Natural REST conventions are strictly preserved:
   - Resource and sub-resource identifiers are represented using `@PathVariable`.
   - Idempotent search, filtering, limits, and pagination parameters on `GET` requests are represented using `@RequestParam`.
   - Action triggers on identified resources (such as starting or completing a quiz session) avoid artificial or redundant request bodies.
3. **Security Architecture Integrity**: Learner identity is strictly derived from the Spring Security context (`Authentication` / `Principal`). Clients are never permitted to provide arbitrary `userId` fields in request bodies.
4. **Validation Rigor**: All request bodies enforce Jakarta Bean Validation constraints (`@NotBlank`, `@NotNull`, `@Size`, `@Positive`, `@PositiveOrZero`, `@Min`, `@Max`, `@Email`).
5. **Thin Controller Architecture**: Controllers remain strictly presentation gateways that validate requests and delegate orchestration to domain services. No business logic, memory algorithms, or database queries reside in controllers.

---

## 2. Controller Inventory

A complete inspection of the backend source code (`com.memora`) identified exactly **18 `@RestController` classes** across **10 domain modules**:

| # | Controller | Package | Endpoints | Architectural Purpose |
| :-: | :--- | :--- | :-: | :--- |
| 1 | `HealthController` | `com.memora.common.controller` | 1 | Liveness diagnostic probe & service metadata |
| 2 | `AuthController` | `com.memora.modules.user.controller` | 2 | Learner registration and JWT authentication |
| 3 | `UserController` | `com.memora.modules.user.controller` | 1 | Current authenticated user profile retrieval |
| 4 | `VocabularyController` | `com.memora.modules.vocabulary.controller` | 7 | Vocabulary catalog lookup, search, CEFR level/category filtering, and word creation |
| 5 | `UserWordProgressController` | `com.memora.modules.vocabulary.controller` | 5 | Learner word retention progress, weak word queries, review scheduling, and initialization |
| 6 | `MemoryController` | `com.memora.modules.memory.controller` | 3 | Spaced repetition memory review recording, due review retrieval, and weak word queries |
| 7 | `QuizController` | `com.memora.modules.quiz.controller` | 6 | Dynamic quiz generation, quiz start/completion lifecycle, answer evaluation, and results |
| 8 | `AssessmentController` | `com.memora.modules.assessment.controller` | 6 | CEFR diagnostic placement assessment lifecycle, answer evaluation, finalization, and history |
| 9 | `LearningPathController` | `com.memora.modules.learningpath.controller` | 8 | Adaptive daily curriculum generation, task start/completion, dynamic path regeneration, day advance, and history |
| 10 | `ProfileController` | `com.memora.modules.gamification.controller` | 4 | Authenticated learner profile summary, paginated XP audit ledger, earned badges, and stats |
| 11 | `LeaderboardController` | `com.memora.modules.gamification.controller` | 1 | Global privacy-preserving learner rankings by XP and streaks |
| 12 | `AchievementController` | `com.memora.modules.gamification.controller` | 1 | System achievement catalog retrieval |
| 13 | `OnboardingController` | `com.memora.modules.onboarding.controller` | 1 | Dynamic onboarding lifecycle state machine status |
| 14 | `DictionaryController` | `com.memora.modules.dictionary.controller` | 3 | Merriam-Webster Collegiate Dictionary real-time lexical lookup (authenticated & public lookup paths) |
| 15 | `AiController` | `com.memora.modules.ai.controller` | 5 | AI vocabulary explanations, contextual examples, mnemonic tips, register/usage analysis, and linguistic relations |
| 16 | `AdaptiveInsightController` | `com.memora.modules.ai.controller` | 1 | Server-side adaptive insights and prioritized daily recommendations |
| 17 | `PartnerController` | `com.memora.modules.partner.controller` | 11 | Learning partner discovery by email/query, requests, progress sharing, pair leaderboard, and activity feed |
| 18 | `VocabularyChallengeController` | `com.memora.modules.partner.challenge.controller` | 9 | Competitive vocabulary duel creation, accept/decline, synchronized questions, answer evaluation, and results |

* **Total Controllers**: **18**
* **Total Endpoints**: **74 distinct handler methods** (**75 mapped HTTP route paths**, with 1 public dictionary route alias `/public-lookup/{word}`)
* **HTTP Method Breakdown**: **43 GET methods** (44 mapped paths), **31 POST methods**, **0 PUT**, **0 PATCH**, **0 DELETE**

---

## 3. Complete Endpoint Audit

Below is the comprehensive audit of all **75 mapped route paths** across the platform:

| # | Controller | Method | Endpoint | Input Type | Request DTO | Validation | Authentication | Status |
| :-: | :--- | :---: | :--- | :--- | :--- | :---: | :---: | :---: |
| 1 | `HealthController` | GET | `/api/v1/health` | No Input | None | N/A | None (Public) | PASS |
| 2 | `AdaptiveInsightController` | GET | `/api/v1/insights/today` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 3 | `AiController` | POST | `/api/v1/ai/word-explanation` | Principal + Request Body | `AiExplanationRequest` | `@Valid` | Required (JWT) | PASS |
| 4 | `AiController` | POST | `/api/v1/ai/example` | Principal + Request Body | `AiExampleRequest` | `@Valid` | Required (JWT) | PASS |
| 5 | `AiController` | POST | `/api/v1/ai/memory-tip` | Principal + Request Body | `AiMemoryTipRequest` | `@Valid` | Required (JWT) | PASS |
| 6 | `AiController` | POST | `/api/v1/ai/contextual-usage` | Principal + Request Body | `AiUsageRequest` | `@Valid` | Required (JWT) | PASS |
| 7 | `AiController` | POST | `/api/v1/ai/word-relations` | Principal + Request Body | `AiWordRelationsRequest` | `@Valid` | Required (JWT) | PASS |
| 8 | `AssessmentController` | POST | `/api/v1/assessments/start` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 9 | `AssessmentController` | GET | `/api/v1/assessments/{assessmentId}` | Path Variable (`assessmentId`) | None | N/A | Required (JWT) | PASS |
| 10 | `AssessmentController` | POST | `/api/v1/assessments/{assessmentId}/questions/{questionId}/answer` | Path + Request Body | `AssessmentAnswerRequest` | `@Valid` | Required (JWT) | PASS |
| 11 | `AssessmentController` | POST | `/api/v1/assessments/{assessmentId}/complete` | Path Variable (`assessmentId`) | None | N/A | Required (JWT) | PASS |
| 12 | `AssessmentController` | GET | `/api/v1/assessments/{assessmentId}/result` | Path Variable (`assessmentId`) | None | N/A | Required (JWT) | PASS |
| 13 | `AssessmentController` | GET | `/api/v1/assessments/history` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 14 | `DictionaryController` | GET | `/api/v1/dictionary/{word}` | Path Variable (`word`) | None | N/A | Required (JWT) | PASS |
| 15 | `DictionaryController` | GET | `/api/v1/dictionary/public/{word}` | Path Variable (`word`) | None | N/A | None (Public) | PASS |
| 16 | `DictionaryController` | GET | `/api/v1/dictionary/public-lookup/{word}` | Path Variable (`word`) | None | N/A | None (Public) | PASS |
| 17 | `AchievementController` | GET | `/api/v1/achievements` | No Input | None | N/A | None (Public) | PASS |
| 18 | `LeaderboardController` | GET | `/api/v1/leaderboard` | Query Parameter (`limit`) | None | N/A | None (Public) | PASS |
| 19 | `ProfileController` | GET | `/api/v1/profile` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 20 | `ProfileController` | GET | `/api/v1/profile/xp-history` | Query Parameter (`page`, `size`) | None | N/A | Required (JWT) | PASS |
| 21 | `ProfileController` | GET | `/api/v1/profile/achievements` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 22 | `ProfileController` | GET | `/api/v1/profile/stats` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 23 | `LearningPathController` | POST | `/api/v1/learning-path/start` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 24 | `LearningPathController` | GET | `/api/v1/learning-path/current` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 25 | `LearningPathController` | GET | `/api/v1/learning-path/today` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 26 | `LearningPathController` | POST | `/api/v1/learning-path/items/{itemId}/start` | Path Variable (`itemId`) | None | N/A | Required (JWT) | PASS |
| 27 | `LearningPathController` | POST | `/api/v1/learning-path/items/{itemId}/complete` | Path + Request Body (Optional) | `LearningItemCompletionRequest` | `@Valid` | Required (JWT) | PASS |
| 28 | `LearningPathController` | POST | `/api/v1/learning-path/regenerate` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 29 | `LearningPathController` | POST | `/api/v1/learning-path/advance` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 30 | `LearningPathController` | GET | `/api/v1/learning-path/history` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 31 | `MemoryController` | POST | `/api/v1/memory/review` | Principal + Request Body | `WordReviewRequest` | `@Valid` | Required (JWT) | PASS |
| 32 | `MemoryController` | GET | `/api/v1/memory/due` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 33 | `MemoryController` | GET | `/api/v1/memory/weak` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 34 | `OnboardingController` | GET | `/api/v1/onboarding/state` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 35 | `VocabularyChallengeController` | POST | `/api/v1/partners/challenges` | Principal + Request Body | `CreateChallengeRequest` | `@Valid` | Required (JWT) | PASS |
| 36 | `VocabularyChallengeController` | GET | `/api/v1/partners/challenges` | Query Parameter (`status`) | None | N/A | Required (JWT) | PASS |
| 37 | `VocabularyChallengeController` | GET | `/api/v1/partners/challenges/{challengeId}` | Path Variable (`challengeId`) | None | N/A | Required (JWT) | PASS |
| 38 | `VocabularyChallengeController` | POST | `/api/v1/partners/challenges/{challengeId}/accept` | Path Variable (`challengeId`) | None | N/A | Required (JWT) | PASS |
| 39 | `VocabularyChallengeController` | POST | `/api/v1/partners/challenges/{challengeId}/decline` | Path Variable (`challengeId`) | None | N/A | Required (JWT) | PASS |
| 40 | `VocabularyChallengeController` | POST | `/api/v1/partners/challenges/{challengeId}/cancel` | Path Variable (`challengeId`) | None | N/A | Required (JWT) | PASS |
| 41 | `VocabularyChallengeController` | GET | `/api/v1/partners/challenges/{challengeId}/questions` | Path Variable (`challengeId`) | None | N/A | Required (JWT) | PASS |
| 42 | `VocabularyChallengeController` | POST | `/api/v1/partners/challenges/{challengeId}/submit` | Path + Request Body | `SubmitChallengeRequest` | `@Valid` | Required (JWT) | PASS |
| 43 | `VocabularyChallengeController` | GET | `/api/v1/partners/challenges/{challengeId}/result` | Path Variable (`challengeId`) | None | N/A | Required (JWT) | PASS |
| 44 | `PartnerController` | GET | `/api/v1/partners/search?email` | Query Parameter (`email`) | None | N/A | Required (JWT) | PASS |
| 45 | `PartnerController` | GET | `/api/v1/partners/search` | Query Parameter (`query`) | None | N/A | Required (JWT) | PASS |
| 46 | `PartnerController` | GET | `/api/v1/partners/leaderboard` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 47 | `PartnerController` | GET | `/api/v1/partners/activity` | Query Parameter (`limit`) | None | N/A | Required (JWT) | PASS |
| 48 | `PartnerController` | GET | `/api/v1/partners` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 49 | `PartnerController` | GET | `/api/v1/partners/requests` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 50 | `PartnerController` | POST | `/api/v1/partners/requests` | Principal + Request Body | `SendPartnerRequestDto` | `@Valid` | Required (JWT) | PASS |
| 51 | `PartnerController` | POST | `/api/v1/partners/requests/{id}/accept` | Path Variable (`id`) | None | N/A | Required (JWT) | PASS |
| 52 | `PartnerController` | POST | `/api/v1/partners/requests/{id}/reject` | Path Variable (`id`) | None | N/A | Required (JWT) | PASS |
| 53 | `PartnerController` | POST | `/api/v1/partners/requests/{id}/cancel` | Path Variable (`id`) | None | N/A | Required (JWT) | PASS |
| 54 | `PartnerController` | GET | `/api/v1/partners/{partnerId}/progress` | Path Variable (`partnerId`) | None | N/A | Required (JWT) | PASS |
| 55 | `QuizController` | POST | `/api/v1/quizzes/generate` | Request Body (Optional) | `QuizGenerationRequest` | `@Valid` | None (Public) | PASS |
| 56 | `QuizController` | POST | `/api/v1/quizzes/{quizId}/start` | Path Variable (`quizId`) | None | N/A | Required (JWT) | PASS |
| 57 | `QuizController` | POST | `/api/v1/quizzes/{quizId}/questions/{questionId}/answer` | Path + Request Body | `AnswerSubmissionRequest` | `@Valid` | Required (JWT) | PASS |
| 58 | `QuizController` | POST | `/api/v1/quizzes/{quizId}/complete` | Path Variable (`quizId`) | None | N/A | Required (JWT) | PASS |
| 59 | `QuizController` | GET | `/api/v1/quizzes/{quizId}` | Path Variable (`quizId`) | None | N/A | None (Public) | PASS |
| 60 | `QuizController` | GET | `/api/v1/quizzes/{quizId}/result` | Path Variable (`quizId`) | None | N/A | Required (JWT) | PASS |
| 61 | `AuthController` | POST | `/api/v1/auth/register` | Request Body | `RegistrationRequest` | `@Valid` | None (Public) | PASS |
| 62 | `AuthController` | POST | `/api/v1/auth/login` | Request Body | `LoginRequest` | `@Valid` | None (Public) | PASS |
| 63 | `UserController` | GET | `/api/v1/users/me` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 64 | `UserWordProgressController` | GET | `/api/v1/progress/words` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 65 | `UserWordProgressController` | GET | `/api/v1/progress/words/{wordId}` | Path Variable (`wordId`) | None | N/A | Required (JWT) | PASS |
| 66 | `UserWordProgressController` | POST | `/api/v1/progress/words/{wordId}/init` | Path Variable (`wordId`) | None | N/A | Required (JWT) | PASS |
| 67 | `UserWordProgressController` | GET | `/api/v1/progress/weak` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 68 | `UserWordProgressController` | GET | `/api/v1/progress/review` | Authentication Principal | None | N/A | Required (JWT) | PASS |
| 69 | `VocabularyController` | GET | `/api/v1/vocabulary` | No Input | None | N/A | None (Public) | PASS |
| 70 | `VocabularyController` | GET | `/api/v1/vocabulary/{id}` | Path Variable (`id`) | None | N/A | None (Public) | PASS |
| 71 | `VocabularyController` | GET | `/api/v1/vocabulary/search` | Query Parameter (`query`) | None | N/A | None (Public) | PASS |
| 72 | `VocabularyController` | GET | `/api/v1/vocabulary/level/{level}` | Path Variable (`level`) | None | N/A | None (Public) | PASS |
| 73 | `VocabularyController` | GET | `/api/v1/vocabulary/category/{category}` | Path Variable (`category`) | None | N/A | None (Public) | PASS |
| 74 | `VocabularyController` | GET | `/api/v1/vocabulary/random` | Query Parameter (`limit`) | None | N/A | None (Public) | PASS |
| 75 | `VocabularyController` | POST | `/api/v1/vocabulary` | Principal + Request Body | `VocabularyWordRequest` | `@Valid` | Required (JWT) | PASS |

---

## 4. Request DTO Inventory

Every Request DTO utilized by backend controllers is documented below with its exact Jakarta validation constraints and target endpoints:

| Request DTO | Package | Controller | Target Endpoint(s) | Purpose | Validation Constraints |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `RegistrationRequest` | `com.memora.modules.user.dto` | `AuthController` | `POST /api/v1/auth/register` | New learner registration | `@NotBlank`, `@Size(min = 2, max = 100)` on `name`<br>`@NotBlank`, `@Email` on `email`<br>`@NotBlank`, `@Size(min = 6, max = 100)` on `password` |
| `LoginRequest` | `com.memora.modules.user.dto` | `AuthController` | `POST /api/v1/auth/login` | Learner authentication | `@NotBlank`, `@Email` on `email`<br>`@NotBlank` on `password` |
| `VocabularyWordRequest` | `com.memora.modules.vocabulary.dto` | `VocabularyController` | `POST /api/v1/vocabulary` | Catalog vocabulary word creation | `@NotBlank`, `@Size(min = 1, max = 100)` on `word`<br>`@NotBlank`, `@Size(max = 255)` on `meaning`<br>`@NotNull` on `difficultyLevel`<br>`@NotNull` on `category` |
| `WordReviewRequest` | `com.memora.modules.memory.dto` | `MemoryController` | `POST /api/v1/memory/review` | Spaced repetition memory review submission | `@NotNull`, `@Positive` on `vocabularyWordId`<br>`@NotNull` on `correct`<br>`@NotNull`, `@Positive` on `responseTimeMs`<br>`@NotNull` on `algorithm` |
| `QuizGenerationRequest` | `com.memora.modules.quiz.dto` | `QuizController` | `POST /api/v1/quizzes/generate` | Algorithmic vocabulary quiz generation | `@Min(1)`, `@Max(50)` on `questionCount` (optional body) |
| `AnswerSubmissionRequest` | `com.memora.modules.quiz.dto` | `QuizController` | `POST /api/v1/quizzes/{quizId}/questions/{questionId}/answer` | Quiz question answer submission & latency | `@NotNull` on `answer`<br>`@NotNull`, `@PositiveOrZero` on `responseTimeMs` |
| `AssessmentAnswerRequest` | `com.memora.modules.assessment.dto` | `AssessmentController` | `POST /api/v1/assessments/{assessmentId}/questions/{questionId}/answer` | Diagnostic placement answer submission & latency | `@NotNull` on `answer`<br>`@NotNull`, `@PositiveOrZero` on `responseTimeMs` |
| `LearningItemCompletionRequest` | `com.memora.modules.learningpath.dto` | `LearningPathController` | `POST /api/v1/learning-path/items/{itemId}/complete` | Curricular learning item completion with review telemetry | `@PositiveOrZero` on `responseTimeMs`<br>`@Size(max = 500)` on `notes` (optional body) |
| `AiExplanationRequest` | `com.memora.modules.ai.dto` | `AiController` | `POST /api/v1/ai/word-explanation` | AI vocabulary pedagogical explanation generation | `@Size(max = 100)` on `word`<br>`isValid()` ensures `wordId > 0` or non-blank `word` |
| `AiExampleRequest` | `com.memora.modules.ai.dto` | `AiController` | `POST /api/v1/ai/example` | AI contextual example sentence generation | `@Size(max = 100)` on `word`<br>`isValid()` ensures `wordId > 0` or non-blank `word` |
| `AiMemoryTipRequest` | `com.memora.modules.ai.dto` | `AiController` | `POST /api/v1/ai/memory-tip` | AI mnemonic retention tip generation | `@Size(max = 100)` on `word`<br>`isValid()` ensures `wordId > 0` or non-blank `word` |
| `AiUsageRequest` | `com.memora.modules.ai.dto` | `AiController` | `POST /api/v1/ai/contextual-usage` | AI collocations, register, and formal/informal usage | `@Size(max = 100)` on `word`<br>`isValid()` ensures `wordId > 0` or non-blank `word` |
| `AiWordRelationsRequest` | `com.memora.modules.ai.dto` | `AiController` | `POST /api/v1/ai/word-relations` | AI linguistic relations (synonyms, antonyms, word families) | `@Size(max = 100)` on `word`<br>`isValid()` ensures `wordId > 0` or non-blank `word` |
| `SendPartnerRequestDto` | `com.memora.modules.partner.dto` | `PartnerController` | `POST /api/v1/partners/requests` | Learning partner pairing request submission | `@NotNull`, `@Positive` on `targetUserId` |
| `CreateChallengeRequest` | `com.memora.modules.partner.challenge.dto` | `VocabularyChallengeController` | `POST /api/v1/partners/challenges` | Vocabulary duel initiation | `@NotNull` on `partnerId`<br>`@NotBlank` on `cefrLevel`<br>`@Min(1)`, `@Max(20)` on `questionCount` |
| `SubmitChallengeRequest` | `com.memora.modules.partner.challenge.dto` | `VocabularyChallengeController` | `POST /api/v1/partners/challenges/{challengeId}/submit` | Competitive challenge duel answer submission | `@NotEmpty` on `answers` (`List<ChallengeAnswerRequest>`) |

* **Total Dedicated Request DTOs Directly Mapped to Endpoints**: **16**
* **Child Item DTOs**: `ChallengeAnswerRequest` (encapsulated inside `SubmitChallengeRequest`) brings the total Request DTO classes in the codebase to **17**.
* **Validation Enforcement**: 100% of Request DTOs contain declared Jakarta validation constraints or custom semantic validation methods.

---

## 5. Structured User Input Compliance

For all endpoints accepting multi-field or structured payloads from the client, Memora strictly enforces dedicated Request DTOs with `@Valid @RequestBody`.

### Key Design Principles:
1. **No Primitive Controller Parameters**: Endpoints never receive loose parameters for data creation/submission.
2. **Optional Request Bodies**:
   - `POST /api/v1/quizzes/generate`: `@Valid @RequestBody(required = false) QuizGenerationRequest request` allows defaulting to A1 level and 5 questions if an empty body is provided.
   - `POST /api/v1/learning-path/items/{itemId}/complete`: `@Valid @RequestBody(required = false) LearningItemCompletionRequest request` permits simple item completion or optional telemetry reporting for review items.
3. **No Redundant DTO Duplication**: Existing domain Request DTOs are reused across matching requirements rather than creating artificial single-use duplicates.

### Detailed Relationship: Request DTO Count (16) vs. Structured Request-Body Endpoint Counts (14 vs 16)
* **Total Top-Level Request DTO Classes**: **16**
* **Endpoints Declaring a Request Object via `@RequestBody`**: **16** (mapped 1-to-1 to the 16 Request DTO classes)
* **Breakdown by Requirement Strictness**:
  * **14 Mandatory Request-Body Endpoints (`required = true` / default)**:
    1. `POST /api/v1/auth/register` (`RegistrationRequest`)
    2. `POST /api/v1/auth/login` (`LoginRequest`)
    3. `POST /api/v1/vocabulary` (`VocabularyWordRequest`)
    4. `POST /api/v1/memory/review` (`WordReviewRequest`)
    5. `POST /api/v1/quizzes/{quizId}/questions/{questionId}/answer` (`AnswerSubmissionRequest`)
    6. `POST /api/v1/assessments/{assessmentId}/questions/{questionId}/answer` (`AssessmentAnswerRequest`)
    7. `POST /api/v1/ai/word-explanation` (`AiExplanationRequest`)
    8. `POST /api/v1/ai/example` (`AiExampleRequest`)
    9. `POST /api/v1/ai/memory-tip` (`AiMemoryTipRequest`)
    10. `POST /api/v1/ai/contextual-usage` (`AiUsageRequest`)
    11. `POST /api/v1/ai/word-relations` (`AiWordRelationsRequest`)
    12. `POST /api/v1/partners/requests` (`SendPartnerRequestDto`)
    13. `POST /api/v1/partners/challenges` (`CreateChallengeRequest`)
    14. `POST /api/v1/partners/challenges/{challengeId}/submit` (`SubmitChallengeRequest`)
  * **2 Optional Request-Body Endpoints (`@Valid @RequestBody(required = false)`)**:
    15. `POST /api/v1/quizzes/generate` (`QuizGenerationRequest`)
    16. `POST /api/v1/learning-path/items/{itemId}/complete` (`LearningItemCompletionRequest`)

---

## 6. Path Variables

Path Variables (`@PathVariable`) are retained strictly for identifying discrete REST resources or triggering lifecycle actions upon existing resources.

### Architectural Justification:
* `/api/v1/vocabulary/{id}`: Directly references a specific `VocabularyWord` entity by primary key.
* `/api/v1/dictionary/{word}`: Identifies the target lookup word in the lexical dictionary path.
* `/api/v1/quizzes/{quizId}` & `/api/v1/quizzes/{quizId}/result`: References a discrete quiz attempt session.
* `/api/v1/assessments/{assessmentId}` & `/api/v1/assessments/{assessmentId}/result`: References a discrete placement assessment session.
* `/api/v1/learning-path/items/{itemId}`: References an individual scheduled item in a learner's curriculum.
* `/api/v1/partners/{partnerId}/progress`: References an accepted partner for privacy-safe progress sharing.
* `/api/v1/partners/challenges/{challengeId}`: References a specific head-to-head vocabulary duel.

### POST Action Endpoints Using Path Variables:
Endpoints such as:
* `POST /api/v1/quizzes/{quizId}/start`
* `POST /api/v1/quizzes/{quizId}/complete`
* `POST /api/v1/assessments/{assessmentId}/complete`
* `POST /api/v1/learning-path/items/{itemId}/start`
* `POST /api/v1/progress/words/{wordId}/init`
* `POST /api/v1/partners/requests/{id}/accept`
* `POST /api/v1/partners/requests/{id}/reject`
* `POST /api/v1/partners/requests/{id}/cancel`
* `POST /api/v1/partners/challenges/{challengeId}/accept`
* `POST /api/v1/partners/challenges/{challengeId}/decline`
* `POST /api/v1/partners/challenges/{challengeId}/cancel`

These endpoints trigger state transitions on existing server-managed entities. The target entity is uniquely identified by the URI path variable, while the actor is resolved from the security context (`Authentication`).

---

## 7. Query Parameters

Query parameters (`@RequestParam`) are used exclusively on idempotent HTTP `GET` endpoints for filtering, search, pagination, and result limits:

1. `GET /api/v1/vocabulary/search?query=eloquent` (`query`)
2. `GET /api/v1/vocabulary/random?limit=10` (`limit`)
3. `GET /api/v1/profile/xp-history?page=0&size=20` (`page`, `size`)
4. `GET /api/v1/leaderboard?limit=20` (`limit`)
5. `GET /api/v1/partners/search?email=alice@test.com` (`email`)
6. `GET /api/v1/partners/search?query=alice` (`query`)
7. `GET /api/v1/partners/activity?limit=20` (`limit`)
8. `GET /api/v1/partners/challenges?status=ACCEPTED` (`status`)

HTTP `GET` requests MUST NOT use request bodies according to RFC 7231 / RFC 9110 HTTP specifications. Query parameters are the standard, correct mechanism for idempotent queries.

---

## 8. Controller Responsibility

Every controller in Memora complies with clean layered architecture:

```text
HTTP Request
     ↓
Controller (@RestController)
  ├── 1. Intercept HTTP method & route mapping
  ├── 2. Trigger Jakarta validation (@Valid)
  ├── 3. Extract authenticated principal (Authentication)
  └── 4. Delegate to domain service
     ↓
Request DTO
     ↓
Domain Service Layer (Transaction boundary, business rules, algorithms)
     ↓
Domain Entities & Repositories (JPA persistence)
     ↓
Response DTO
     ↓
Controller wraps in ResponseEntity<ApiResponse<T>>
     ↓
HTTP Response
```

### Architectural Guarantees:
* **No Direct DB Calls**: Controllers never inject or invoke Spring Data repositories.
* **No Business Logic**: Spaced repetition math (SM-2, Leitner), CEFR placement math, quiz evaluation strategies, and streak calculations are encapsulated entirely within dedicated service/strategy classes.
* **Statelessness**: Controllers maintain no session or mutable instance state.

---

## 9. Tests & Verification

All automated verification commands were executed directly against the workspace:

### 1. Backend Automated Test Suite
```bash
.\mvnw.cmd test
```
* **Results**: **Tests run: 384, Failures: 0, Errors: 0, Skipped: 0**
* **Status**: **BUILD SUCCESS**
* **Coverage**: All 58 test classes across all modules passed cleanly, including core module controller integration test suites, partner integration tests, horizontal privilege escalation tests, and MockMvc controller test suites.
* All 384 automated tests pass with 0 failures, 0 errors, and 0 skipped tests.

### 2. Frontend TypeScript Verification
```bash
npx tsc --noEmit
```
* **Results**: **0 errors, exit code 0**
* **Status**: Clean compilation; all TypeScript request types mirror backend DTO schemas.

### 3. Frontend Production Build
```bash
npm run build
```
* **Results**: **2536 modules transformed**, production bundle created in `dist/` with exit code 0.
* **Status**: **Successful production build, exit code 0**.

---

## 10. Compliance Summary

| Requirement | Status | Evidence |
| :--- | :---: | :--- |
| **All Controllers Audited** | **PASS** | 18 of 18 controllers inspected and verified across all modules. |
| **Structured Inputs Use Request DTOs** | **PASS** | 100% of structured input endpoints (16 endpoints: 14 mandatory + 2 optional `required = false`) use dedicated Request DTOs. |
| **Request Validation Implemented** | **PASS** | `@Valid` on all request bodies; Jakarta constraints on all 16 Request DTOs. |
| **Path Variables Properly Used** | **PASS** | All path variable endpoints use `@PathVariable` strictly for entity IDs and lifecycle actions on existing entities. |
| **Query Parameters Properly Used** | **PASS** | Query parameters used strictly for search, pagination, limits, and status filtering on GET. |
| **Authentication Derived from Security Context** | **PASS** | 100% of authenticated endpoints resolve identity from Spring Security `Authentication`. No client-supplied `userId`. |
| **Controller Tests Pass** | **PASS** | 384 of 384 tests passed across all 58 test classes. |
| **Frontend Regression Check** | **PASS** | TypeScript type check and Vite production build passed with 0 errors. |

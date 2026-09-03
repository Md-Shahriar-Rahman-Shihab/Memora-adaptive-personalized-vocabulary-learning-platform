# Memora – AI Memory-Based Adaptive Vocabulary Learning Platform

An AI-powered, memory-adaptive personalized vocabulary learning platform engineered with modern enterprise Java and React, strictly adhering to **Advanced Object-Oriented Programming (OOP)** principles, design patterns, and clean architecture.

---

## 📌 Project Status

| Phase | Module | Status | Description |
| :--- | :--- | :--- | :--- |
| **Phase 1** | **Core Architecture & Scaffolding** | ✅ Completed | BaseEntity auditing, unified `ApiResponse<T>`, global exception handling, health checks. |
| **Phase 2** | **Security & Identity Management** | ✅ Completed | Stateless JWT authentication, BCrypt password hashing, Spring Security filter chain, `/api/v1/auth/*`, `/api/v1/users/me`. |
| **Phase 3** | **Vocabulary & Content Catalog** | ✅ Completed | Curated CEFR vocabulary dataset, data seeder, category filtering, `UserWordProgress` entity & progress tracking. |
| **Phase 4** | **Adaptive Memory SRS Engine** | ✅ Completed | **Strategy Pattern** (`SM2MemoryStrategy`, `LeitnerMemoryStrategy`), **Factory Pattern** (`MemoryStrategyFactory`), incremental latency tracking, mastery scoring, forgetting risk evaluation, `/api/v1/memory/*`. |
| **Phase 5** | **Quiz & Evaluation Engine** | ✅ Completed | **Polymorphic Question Models** (`MultipleChoiceQuestion`, `TranslationQuestion`, `FillInTheBlankQuestion`), **Factory Pattern** (`QuestionFactory`), **Strategy Pattern** (`QuestionEvaluatorStrategy`, `QuestionEvaluatorFactory`), attempt history, Memory Engine integration, `/api/v1/quizzes/*`. |
| **Phase 6** | **Assessment & Placement Engine** | ✅ Completed | 20-question multi-tier CEFR diagnostic assessment (`A1`–`C1`), **Strategy Pattern** (`PlacementAlgorithmStrategy`, `DefaultPlacementStrategy`, `PlacementStrategyFactory`), explainable confidence score calculation (0–100), response latency tracking, diagnostic attempt history, memory isolation, `/api/v1/assessments/*`. |
| **Phase 7** | **Adaptive Learning Path Engine** | ✅ Completed | Personalized daily curriculum (max 10 items), **Strategy Pattern** (`LearningPathStrategy`, `AdaptiveLearningPathStrategy`), **Factory Pattern** (`LearningPathStrategyFactory`), dynamic review/new word balancing, CEFR stretch words, consolidating quizzes, progress tracking, dynamic regeneration, `/api/v1/learning-path/*`. |
| **Phase 8** | **Gamification & Event Bus** | ⏳ Planned | XP, streaks, badges, leaderboards, Spring event decoupling. |
| **Phase 9** | **AI Provider Integration** | ⏳ Planned | LLM adapters, sentence and mnemonic generation. |
| **Phase 10** | **React Frontend & UX** | ⏳ Planned | Modern responsive web application with dark mode & micro-animations. |

---

## 🛠️ Technology Stack

### Backend
* **Language & Runtime**: Java 20 / Java 21 (LTS)
* **Framework**: Spring Boot 3.3.4
* **Core Modules**: Spring Web (REST API), Spring Data JPA, Spring Security (Stateless JWT Authentication), Spring Validation
* **Database**: PostgreSQL (Production) / H2 In-Memory (Automated Testing)
* **Build Tool**: Apache Maven (`mvnw` / `mvnw.cmd`)
* **Architecture**: Layered Clean Architecture (`Controller → Service → Repository → Entity/Domain` with strict DTO boundaries)

---

## 📦 Active Backend Package Architecture (`com.memora`)

```text
com.memora/
├── MemoraApplication.java
├── common/
│   ├── controller/                 # HealthController (/api/v1/health)
│   ├── domain/                     # BaseEntity (Audit timestamps: createdAt, updatedAt, version)
│   ├── exception/                  # GlobalExceptionHandler, ResourceNotFoundException, BadRequestException, ApiException
│   └── response/                   # ApiResponse<T>, ErrorDetails, ValidationError
│
├── security/                       # Security & JWT Infrastructure
│   ├── jwt/                        # JwtTokenProvider, JwtAuthenticationFilter, JwtAuthenticationEntryPoint
│   ├── user/                       # CustomUserDetailsService, UserPrincipal
│   └── SecurityConfig.java         # Stateless security filter chain
│
└── modules/
    ├── user/                       # Identity & Learner Domain
    │   ├── domain/                 # Role, VocabularyLevel
    │   ├── dto/                    # RegistrationRequest, LoginRequest, AuthResponse, UserProfileResponse
    │   ├── entity/                 # User entity
    │   ├── repository/             # UserRepository
    │   └── service/                # UserService, UserServiceImpl
    │
    ├── vocabulary/                 # Lexical Catalog & Content
    │   ├── domain/                 # DifficultyLevel, WordCategory, ForgettingRisk
    │   ├── dto/                    # VocabularyWordRequest, VocabularyWordResponse, UserWordProgressResponse
    │   ├── entity/                 # VocabularyWord, UserWordProgress
    │   ├── repository/             # VocabularyWordRepository, UserWordProgressRepository
    │   └── service/                # VocabularyService, UserWordProgressService
    │
    ├── memory/                     # Adaptive Spaced Repetition Engine (Phase 4)
    │   ├── domain/                 # MemoryAlgorithmType (SM2, LEITNER), MemoryCalculationResult
    │   ├── dto/                    # WordReviewRequest, WordReviewResponse, MemoryWordResponse
    │   ├── service/                # MemoryService, MemoryServiceImpl (orchestration layer)
    │   ├── strategy/               # Strategy Pattern for SRS algorithms
    │   │   ├── MemoryAlgorithmStrategy.java   # Strategy interface contract
    │   │   ├── SM2MemoryStrategy.java         # SuperMemo-2 implementation (E-Factor & interval expansion)
    │   │   ├── LeitnerMemoryStrategy.java     # 5-Box Leitner system implementation
    │   │   └── MemoryStrategyFactory.java     # Strategy resolution factory
    │   └── controller/             # MemoryController (/api/v1/memory/*)
    │
    ├── quiz/                       # Quiz & Question Engine (Phase 5)
    │   ├── domain/                 # QuestionType (MULTIPLE_CHOICE, TRANSLATION, FILL_IN_THE_BLANK)
    │   ├── dto/                    # QuizGenerationRequest, QuizResponse, QuestionResponse, AnswerSubmissionRequest, AnswerResponse, QuizResultResponse, EvaluationResult
    │   ├── entity/                 # Polymorphic Question entities & Historical Attempts
    │   │   ├── Question.java (abstract base entity)
    │   │   ├── MultipleChoiceQuestion.java
    │   │   ├── TranslationQuestion.java
    │   │   ├── FillInTheBlankQuestion.java
    │   │   ├── Quiz.java
    │   │   ├── QuizAttempt.java
    │   │   └── QuestionAttempt.java
    │   ├── factory/                # Factory Pattern implementations
    │   │   ├── QuestionFactory.java           # Instantiates polymorphic Question subtypes
    │   │   └── QuestionEvaluatorFactory.java  # Resolves QuestionEvaluatorStrategy by QuestionType
    │   ├── repository/             # QuizRepository, QuestionRepository, QuizAttemptRepository, QuestionAttemptRepository
    │   ├── service/                # QuizService, QuizServiceImpl (orchestration & memory integration)
    │   ├── strategy/               # Strategy Pattern for polymorphic answer evaluation
    │   │   ├── QuestionEvaluatorStrategy.java # Evaluator contract
    │   │   ├── MultipleChoiceEvaluator.java
    │   │   ├── TranslationEvaluator.java
    │   │   └── FillInTheBlankEvaluator.java
    │   └── controller/             # QuizController (/api/v1/quizzes/*)
    │
    ├── assessment/                 # Assessment & Diagnostic Placement Engine (Phase 6)
    │   ├── domain/                 # AssessmentStatus, AssessmentPerformance, PlacementResult
    │   ├── dto/                    # AssessmentStartResponse, AssessmentDetailResponse, AssessmentQuestionResponse, AssessmentAnswerRequest, AssessmentAnswerResponse, PlacementResultResponse
    │   ├── entity/                 # Diagnostic entities
    │   │   ├── Assessment.java                # Assessment session & final level estimation
    │   │   ├── AssessmentQuestion.java        # Question presentation order & CEFR level mapping
    │   │   └── AssessmentAnswer.java          # Learner response, correctness, & response latency
    │   ├── factory/                # PlacementStrategyFactory
    │   ├── repository/             # AssessmentRepository, AssessmentQuestionRepository, AssessmentAnswerRepository
    │   ├── service/                # AssessmentService, AssessmentServiceImpl, AssessmentQuestionGenerator
    │   ├── strategy/               # Strategy Pattern for CEFR placement algorithms
    │   │   ├── PlacementAlgorithmStrategy.java
    │   │   └── DefaultPlacementStrategy.java  # Deterministic CEFR proficiency estimation & confidence score
    │   └── controller/             # AssessmentController (/api/v1/assessments/*)
    │
    └── learningpath/               # Adaptive Learning Path Engine (Phase 7)
        ├── domain/                 # LearningPathStatus, LearningItemType, LearningItemStatus, LearningItemPriority, LearningPathStrategyType, VocabularyWordSummary, LearningPathItemCandidate, LearningPathContext, LearningPathResult
        ├── dto/                    # LearningPathResponse, TodayLearningPathResponse, LearningPathItemResponse, LearningItemCompletionRequest, LearningItemCompletionResponse
        ├── entity/                 # LearningPath, LearningPathItem
        ├── factory/                # LearningPathStrategyFactory
        ├── repository/             # LearningPathRepository, LearningPathItemRepository
        ├── service/                # LearningPathService, LearningPathServiceImpl
        ├── strategy/               # Strategy Pattern for curriculum generation
        │   ├── LearningPathStrategy.java
        │   └── AdaptiveLearningPathStrategy.java # Dynamic review/new word ratio balancing & stretch word logic
        └── controller/             # LearningPathController (/api/v1/learning-path/*)
```

---

## 🏛️ Domain Entities & Database Schema

```mermaid
erDiagram
    User ||--o{ UserWordProgress : "tracks retention"
    VocabularyWord ||--o{ UserWordProgress : "referenced in"
    
    User ||--o{ QuizAttempt : "attempts"
    Quiz ||--o{ QuizAttempt : "attempted in"
    QuizAttempt ||--o{ QuestionAttempt : "contains"
    Quiz ||--o{ Question : "consists of"
    VocabularyWord ||--o{ Question : "evaluates"
    
    Question ||--|{ MultipleChoiceQuestion : "is subtype"
    Question ||--|{ TranslationQuestion : "is subtype"
    Question ||--|{ FillInTheBlankQuestion : "is subtype"

    User ||--o{ Assessment : "takes diagnostic"
    Assessment ||--o{ AssessmentQuestion : "contains (20 Qs across A1-C1)"
    Assessment ||--o{ AssessmentAnswer : "records answers"
    Question ||--o{ AssessmentQuestion : "referenced in"
    AssessmentQuestion ||--o{ AssessmentAnswer : "evaluated in"

    User ||--o{ LearningPath : "enrolled in"
    LearningPath ||--o{ LearningPathItem : "schedules (daily curriculum)"
    VocabularyWord ||--o{ LearningPathItem : "studied in"
    Quiz ||--o{ LearningPathItem : "consolidated in"

    User {
        bigint id PK
        varchar name
        varchar email UK
        varchar password_hash
        varchar current_level
        int xp
        int streak
        varchar role
        timestamp created_at
        timestamp updated_at
        bigint version
    }

    VocabularyWord {
        bigint id PK
        varchar word UK
        varchar meaning
        text definition
        varchar pronunciation
        text example_sentence
        varchar difficulty_level
        varchar category
        timestamp created_at
        timestamp updated_at
        bigint version
    }

    UserWordProgress {
        bigint id PK
        bigint user_id FK
        bigint vocabulary_word_id FK
        double mastery_score
        varchar forgetting_risk
        int total_attempts
        int correct_attempts
        int incorrect_attempts
        int consecutive_correct
        double ease_factor
        int review_interval_days
        int leitner_box
        timestamp next_review_at
        timestamp last_reviewed_at
        double average_response_time
        timestamp created_at
        timestamp updated_at
        bigint version
    }

    Quiz {
        bigint id PK
        varchar title
        varchar difficulty_level
        int total_questions
        timestamp created_at
        timestamp updated_at
        bigint version
    }

    Question {
        bigint id PK
        bigint quiz_id FK
        bigint vocabulary_word_id FK
        varchar question_type
        int points
        text question_text
        timestamp created_at
        timestamp updated_at
        bigint version
    }

    MultipleChoiceQuestion {
        bigint id PK
        varchar correct_option
    }

    TranslationQuestion {
        bigint id PK
        varchar expected_answer
    }

    FillInTheBlankQuestion {
        bigint id PK
        text sentence
        varchar expected_answer
    }

    QuizAttempt {
        bigint id PK
        bigint user_id FK
        bigint quiz_id FK
        int total_score
        int correct_answers
        int total_questions
        timestamp started_at
        timestamp completed_at
        timestamp created_at
        timestamp updated_at
        bigint version
    }

    QuestionAttempt {
        bigint id PK
        bigint quiz_attempt_id FK
        bigint question_id FK
        text user_answer
        boolean is_correct
        int score
        bigint response_time_ms
        timestamp answered_at
        timestamp created_at
        timestamp updated_at
        bigint version
    }

    Assessment {
        bigint id PK
        bigint user_id FK
        varchar status
        timestamp started_at
        timestamp completed_at
        varchar estimated_level
        double confidence_score
        int total_questions
        int correct_answers
        int score
        double accuracy
        text level_performance_json
        timestamp created_at
        timestamp updated_at
        bigint version
    }

    AssessmentQuestion {
        bigint id PK
        bigint assessment_id FK
        bigint question_id FK
        varchar difficulty_level
        int order_index
        timestamp created_at
        timestamp updated_at
        bigint version
    }

    AssessmentAnswer {
        bigint id PK
        bigint assessment_id FK
        bigint assessment_question_id FK
        text user_answer
        boolean is_correct
        bigint response_time_ms
        timestamp answered_at
        timestamp created_at
        timestamp updated_at
        bigint version
    }

    LearningPath {
        bigint id PK
        bigint user_id FK
        varchar status
        varchar target_level
        int current_day
        int total_items
        int completed_items
        timestamp created_at
        timestamp updated_at
        bigint version
    }

    LearningPathItem {
        bigint id PK
        bigint learning_path_id FK
        bigint vocabulary_word_id FK
        bigint quiz_id FK
        varchar item_type
        varchar priority
        varchar status
        timestamp scheduled_at
        timestamp completed_at
        int order_index
        text notes
        timestamp created_at
        timestamp updated_at
        bigint version
    }
```

---

## 🧬 OOP Principles & Design Patterns Implemented

### 1. Polymorphism & Abstraction (Question Hierarchy)
* **`Question` Abstract Base Entity**: Unifies common question state (`vocabularyWord`, `points`, `questionType`, `questionText`, `quiz`) and prevents incomplete direct instantiation.
* **Concrete Subtypes**:
  * `MultipleChoiceQuestion`: Encapsulates options list (`@ElementCollection`) and `correctOption`.
  * `TranslationQuestion`: Encapsulates `expectedAnswer`.
  * `FillInTheBlankQuestion`: Encapsulates sentence structure with blank marker (`___`) and `expectedAnswer`.

### 2. Strategy Pattern (Polymorphic Question Evaluation)
* **`QuestionEvaluatorStrategy`**: Interface defining `evaluate(Question, String)` returning `EvaluationResult`.
* **Concrete Evaluators**: `MultipleChoiceEvaluator`, `TranslationEvaluator`, `FillInTheBlankEvaluator` implement type-specific validation rules, case-insensitivity, whitespace trimming, and feedback explanation.
* **`QuestionEvaluatorFactory`**: Automatically discovers and maps evaluator strategies by `QuestionType`, eliminating conditional `if-else`/`switch` branching in `QuizService`.

### 3. Factory Pattern (Question Creation)
* **`QuestionFactory`**: Encapsulates the instantiation of polymorphic `Question` subtypes, generating distractor pool choices for MCQs and sentence blanks for fill-in-the-blank questions.

### 4. Spaced Repetition Strategy & Memory Integration
* **`MemoryAlgorithmStrategy`** & **`MemoryStrategyFactory`**: Pluggable SM-2 (`SM2MemoryStrategy`) and Leitner (`LeitnerMemoryStrategy`) algorithms.
* **Quiz → Memory Flow**: Submitting each quiz answer automatically triggers `MemoryService.recordReview(...)` (defaulting to SM-2), instantly updating `UserWordProgress` (mastery score, forgetting risk, next review date) without duplicating memory logic.

### 5. Encapsulation & Historical Persistence
* Historical learner attempts are permanently preserved in `QuizAttempt` and `QuestionAttempt` (recording latency `responseTimeMs`, points, correctness, and timestamps) without overwriting past performance.
* Similarly, `Assessment`, `AssessmentQuestion`, and `AssessmentAnswer` record full diagnostic histories, allowing learners to re-assess later while preserving historical placement milestones.

### 6. Strategy Pattern & Diagnostic Placement Engine (Phase 6)
* **`PlacementAlgorithmStrategy`**: Pluggable algorithm contract `calculate(List<AssessmentPerformance>)` producing `PlacementResult`.
* **`DefaultPlacementStrategy`**: Rule-based deterministic estimation evaluating sequential CEFR mastery thresholds (`A1` → `A2` → `B1` → `B2` → `C1`) at a 75% accuracy threshold. Computes an explainable confidence score (0–100) based on sample completeness, response latency plausibility, boundary separation, and natural language decay consistency.
* **`PlacementStrategyFactory`**: Dynamically resolves placement strategies without hardcoding score branching in the service layer.
* **Separation of Concerns & Memory Isolation**: Diagnostic assessment questions isolate testing from spaced repetition retention tracking (`MemoryService` is NOT invoked during diagnostic assessments).

### 7. Strategy & Factory Pattern in Adaptive Learning Path Engine (Phase 7)
* **`LearningPathStrategy`**: Strategy interface defining `generatePath(LearningPathContext)`.
* **`AdaptiveLearningPathStrategy`**: Adaptive curriculum generation balancing up to 10 daily items:
  - Dynamically computes ratio between reviews, new vocabulary, and consolidating quizzes based on live `forgettingRisk`, `masteryScore`, and `dueReviews`.
  - Introduces advanced "stretch" vocabulary from `targetLevel + 1` when learner demonstrates >= 90% mastery.
  - Automatically appends a consolidating daily quiz task linked to `QuizService`.
* **`LearningPathStrategyFactory`**: Resolves learning path strategies via Spring dependency injection without conditional branching or `instanceof` checks.
* **Dynamic Regeneration**: Pending items can be regenerated on-demand (`POST /api/v1/learning-path/regenerate`) based on real-time memory metrics without deleting completed learning history.

---

## 🌐 Implemented REST API Endpoints

### 1. Authentication (`/api/v1/auth`)
* `POST /api/v1/auth/register` — Register a new learner account
* `POST /api/v1/auth/login` — Authenticate and retrieve JWT token

### 2. User Profile (`/api/v1/users`)
* `GET  /api/v1/users/me` — Retrieve active learner profile and stats (Requires JWT)

### 3. Vocabulary Catalog (`/api/v1/vocabulary`)
* `GET  /api/v1/vocabulary` — List all vocabulary words (Public)
* `GET  /api/v1/vocabulary/{id}` — Get word definition and metadata (Public)
* `GET  /api/v1/vocabulary/search?query=...` — Search vocabulary by keyword (Public)
* `GET  /api/v1/vocabulary/level/{level}` — Filter by CEFR difficulty level (`A1`–`C2`) (Public)
* `GET  /api/v1/vocabulary/category/{category}` — Filter by topic category (Public)
* `POST /api/v1/vocabulary` — Create a new vocabulary word (Admin/Authenticated)

### 4. User Word Progress (`/api/v1/progress`)
* `GET  /api/v1/progress/words` — List all progress records for authenticated user
* `GET  /api/v1/progress/words/{wordId}` — Get progress for a specific word
* `POST /api/v1/progress/words/{wordId}/init` — Initialize progress tracking for a word
* `GET  /api/v1/progress/weak` — Retrieve learner's weak words
* `GET  /api/v1/progress/review` — Retrieve words due for review

### 5. Adaptive Memory Engine (`/api/v1/memory`)
* `POST /api/v1/memory/review` — Record learner review attempt and calculate next review schedule
* `GET  /api/v1/memory/due` — Retrieve vocabulary words due for spaced repetition review (`nextReviewAt <= now`)
* `GET  /api/v1/memory/weak` — Retrieve learner's weakest words ranked by forgetting risk and mastery score

### 6. Quiz & Question Engine (`/api/v1/quizzes`)
* `POST /api/v1/quizzes/generate` — Deterministically generate a new quiz from vocabulary pool
* `POST /api/v1/quizzes/{quizId}/start` — Start or resume a quiz attempt session
* `POST /api/v1/quizzes/{quizId}/questions/{questionId}/answer` — Submit answer, evaluate, record latency, update memory retention
* `POST /api/v1/quizzes/{quizId}/complete` — Complete quiz attempt and calculate total score/percentage
* `GET  /api/v1/quizzes/{quizId}` — Retrieve quiz details and questions (omits correct answers)
* `GET  /api/v1/quizzes/{quizId}/result` — Retrieve latest attempt score and performance summary

### 7. Assessment & Placement Engine (`/api/v1/assessments`)
* `POST /api/v1/assessments/start` — Start a new 20-question diagnostic assessment (4 questions each from A1, A2, B1, B2, C1)
* `GET  /api/v1/assessments/{assessmentId}` — Retrieve assessment details, progress, and questions without answers
* `POST /api/v1/assessments/{assessmentId}/questions/{questionId}/answer` — Submit answer and latency for a diagnostic question
* `POST /api/v1/assessments/{assessmentId}/complete` — Complete assessment, execute placement algorithm, update learner CEFR level
* `GET  /api/v1/assessments/{assessmentId}/result` — Retrieve placement result, CEFR level estimate, and confidence score
* `GET  /api/v1/assessments/history` — Retrieve all completed historical placement assessments for the learner

### 8. Adaptive Learning Path Engine (`/api/v1/learning-path`)
* `POST /api/v1/learning-path/start` — Start or resume the active personalized learning path
* `GET  /api/v1/learning-path/current` — Retrieve active learning path summary and item status
* `GET  /api/v1/learning-path/today` — Retrieve today's prioritized learning tasks
* `POST /api/v1/learning-path/items/{itemId}/start` — Mark a learning path item as in-progress
* `POST /api/v1/learning-path/items/{itemId}/complete` — Mark an item as completed and trigger Memory Engine review if applicable
* `POST /api/v1/learning-path/regenerate` — Dynamically regenerate pending items based on latest memory state
* `GET  /api/v1/learning-path/history` — Retrieve historical learning path records for the learner

---

## 📝 Example Learning Path Workflow API Usage

### 1. Start Learning Path: `POST /api/v1/learning-path/start`
**Headers**: `Authorization: Bearer <JWT_TOKEN>`  
**Response**:
```json
{
  "success": true,
  "message": "Learning path initiated successfully",
  "data": {
    "id": 1,
    "status": "ACTIVE",
    "targetLevel": "B1",
    "currentDay": 1,
    "totalItems": 10,
    "completedItems": 0,
    "items": [
      {
        "id": 1,
        "type": "REVIEW",
        "wordId": 4,
        "word": "meticulous",
        "meaning": "showing great attention to detail",
        "priority": "HIGH",
        "status": "PENDING",
        "orderIndex": 1,
        "notes": "Review due (HIGH risk, 45.0% mastery)"
      },
      {
        "id": 2,
        "type": "NEW_WORD",
        "wordId": 12,
        "word": "lucid",
        "meaning": "expressed clearly; easy to understand",
        "priority": "MEDIUM",
        "status": "PENDING",
        "orderIndex": 2,
        "notes": "New vocabulary discovery (CEFR B1)"
      },
      {
        "id": 10,
        "type": "QUIZ",
        "quizId": 3,
        "priority": "HIGH",
        "status": "PENDING",
        "orderIndex": 10,
        "notes": "Daily comprehensive retention & diagnostic quiz"
      }
    ]
  }
}
```

### 2. Complete a Review Item: `POST /api/v1/learning-path/items/1/complete`
**Headers**: `Authorization: Bearer <JWT_TOKEN>`  
**Body**:
```json
{
  "correct": true,
  "responseTimeMs": 1400,
  "algorithm": "SM2"
}
```
**Response**:
```json
{
  "success": true,
  "message": "Item completed successfully",
  "data": {
    "itemId": 1,
    "status": "COMPLETED",
    "itemType": "REVIEW",
    "completedAt": "2026-09-03T12:00:00Z",
    "pathCompleted": false,
    "completedItems": 1,
    "totalItems": 10,
    "message": "Learning item completed successfully"
  }
}
```

---

## 💻 Getting Started (Backend)

### 1. Prerequisites
* **Java**: JDK 20 or 21 installed (`java -version`)
* **Database**: PostgreSQL 14+ (or runs seamlessly on H2 in-memory for testing)
* **Build Tool**: Maven Wrapper (`.\mvnw.cmd` on Windows, `./mvnw` on Linux/macOS)

### 2. Build & Run Application
```powershell
# Compile and package
.\mvnw.cmd clean package

# Run Spring Boot backend
.\mvnw.cmd spring-boot:run
```

### 3. Running Automated Tests
```powershell
# Run the complete test suite (111 tests across Phase 1–7 modules)
.\mvnw.cmd clean test
```

### 4. Health Check Verification
```bash
curl -X GET http://localhost:8080/api/v1/health
```

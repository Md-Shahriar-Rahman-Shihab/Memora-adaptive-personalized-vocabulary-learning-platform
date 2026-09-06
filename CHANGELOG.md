# Memora – Development Changelog

## Date: September 6–7, 2026

This document provides a comprehensive, centralized record of all fixes, architectural refinements, and feature enhancements made to the Memora platform today across both the Spring Boot backend and the React frontend.

---

## 📋 Executive Summary

| Category | Primary Issue / Goal | Solution / Architecture Update | Status |
| :--- | :--- | :--- | :--- |
| **Assessment Query Bug** | `IncorrectResultSizeDataAccessException` when querying user's latest assessment | Refactored `AssessmentRepository` to use `findFirstByUserIdOrderByCreatedAtDesc` and `List<Assessment>` fallback to eliminate non-unique result exceptions | ✅ Resolved |
| **Blank Pages / Navigation** | White screen when clicking Leaderboard or Profile navigation links | Added null-safe property accesses, proper type mappings for `LeaderboardEntryResponse` and `XpTransactionResponse`, and error resilience in React pages | ✅ Resolved |
| **Vocabulary Learning Workflow** | Clicking word mark-complete instantly completed it without user review | Introduced an interactive vocabulary study pop-up modal showing word definitions, CEFR tier, examples, and pronunciation with an explicit "Mark as Complete (+15 XP)" action | ✅ Implemented |
| **Modal UI & CSS Styling** | Dark backdrop blur obscured background content and looked unpolished | Refined `Modal.tsx` CSS: removed excessive backdrop blur, centered card layout, improved border styling, padding, and smooth entrance transitions | ✅ Resolved |
| **Sidebar Layout** | Awkward empty white space between navigation items and user profile footer | Set fixed-height flex column (`h-screen flex flex-col justify-between`) with scrollable nav link container and anchored user profile/sign-out card | ✅ Resolved |
| **Daily Retention Quiz Sync** | Completing the daily quiz did not mark the quiz item as complete or advance to the next day's path | Overhauled `QuizServiceImpl.java` completion logic: resolved lazy-loading exceptions, queried active path items directly via repository, marked item and path as `COMPLETED` | ✅ Resolved |
| **Learning Path Progression** | Daily learning path remained static after finishing all items | Auto-advancement trigger: `getTodayPath` detects completed paths and automatically instantiates the next day's curriculum | ✅ Resolved |
| **XP Consistency Alignment** | Word completion awarded 15 XP on backend but displayed +20 XP on frontend; quiz XP was hardcoded to +40 XP | Aligned word completion display to **+15 XP** (`LESSON_COMPLETION_XP = 15`); dynamically calculated quiz XP based on correct answers (10 XP/correct + 20 XP perfect score bonus) matching `QuizRewardStrategy` | ✅ Resolved |

---

## 🛠️ Detailed File Changes & Technical Breakdown

### 1. Assessment Module
- **[`src/main/java/com/memora/modules/assessment/repository/AssessmentRepository.java`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/src/main/java/com/memora/modules/assessment/repository/AssessmentRepository.java)**
  - **Issue**: Multiple assessment records for a user caused Spring Data JPA's derived single-result query to throw `IncorrectResultSizeDataAccessException: Query did not return a unique result: 2 results were returned`.
  - **Fix**: Replaced single-entity query with `Optional<Assessment> findFirstByUserIdOrderByCreatedAtDesc(Long userId)` and added list-based lookup methods to guarantee deterministic top-1 retrieval.
- **[`frontend/src/pages/AssessmentResultPage.tsx`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/frontend/src/pages/AssessmentResultPage.tsx)**
  - Added safe handling for assessment result navigation, null checks for placement levels, and smooth redirect to the adaptive learning path.

---

### 2. Gamification, Profile & Leaderboard
- **[`src/main/java/com/memora/modules/gamification/dto/LeaderboardEntryResponse.java`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/src/main/java/com/memora/modules/gamification/dto/LeaderboardEntryResponse.java)**
  - Added missing fields and constructors for serialization of rank, user displayName, avatarUrl, streak count, and total XP.
- **[`src/main/java/com/memora/modules/gamification/dto/XpTransactionResponse.java`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/src/main/java/com/memora/modules/gamification/dto/XpTransactionResponse.java)**
  - Ensured all transaction metadata (activity type, XP delta, timestamp) is serializable with backward-compatible defaults.
- **[`frontend/src/types/gamification.ts`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/frontend/src/types/gamification.ts)**
  - Updated TypeScript interfaces (`LeaderboardEntry`, `XpTransaction`, `GamificationProfile`) with optional null-safe fields.
- **[`frontend/src/pages/LeaderboardPage.tsx`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/frontend/src/pages/LeaderboardPage.tsx)**
  - Prevented crash on undefined leaderboard data by introducing loading skeletons, empty state fallback, and defensive array checks.
- **[`frontend/src/pages/ProfilePage.tsx`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/frontend/src/pages/ProfilePage.tsx)**
  - Fixed white-screen crash by safely unpacking profile responses, handling missing achievement lists, and rendering user stats cleanly.

---

### 3. Vocabulary Learning Workflow & Modal Polish
- **[`frontend/src/pages/LearningPathPage.tsx`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/frontend/src/pages/LearningPathPage.tsx)**
  - **Learning Modal Flow**: When clicking on an incomplete vocabulary lesson card, a study modal pops up showing the word, part of speech, definition, CEFR badge, example sentence with highlight, and audio playback placeholder.
  - **Action Button**: Inside the modal, users can click **"Mark as Complete (+15 XP)"** to commit the word learning, trigger confetti/toast feedback, and mark the item completed in the curriculum.
  - **XP Display Update**: Corrected hardcoded `+20 XP` labels to `+15 XP` across the page and toast alerts.
- **[`frontend/src/components/ui/Modal.tsx`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/frontend/src/components/ui/Modal.tsx)**
  - Replaced heavy `backdrop-blur-md` with subtle dark overlay (`bg-black/40`) to keep background readable without distortion.
  - Centered card positioning and ensured clean responsive padding for mobile and desktop.

---

### 4. Layout & Sidebar Polish
- **[`frontend/src/components/layout/Sidebar.tsx`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/frontend/src/components/layout/Sidebar.tsx)**
  - Transformed sidebar into fixed layout: `h-screen fixed left-0 top-0 flex flex-col justify-between`.
  - Structured navigation links in a flex-1 container with dedicated overflow handling.
  - Locked the user account details and Sign Out action into a distinct, permanent bottom card without excess blank space.

---

### 5. Learning Path & Adaptive Progression Engine
- **[`src/main/java/com/memora/modules/learningpath/dto/LearningPathItemResponse.java`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/src/main/java/com/memora/modules/learningpath/dto/LearningPathItemResponse.java)**
  - Added extended vocabulary details (definition, example, CEFR level, phonetic transcription) so the frontend modal can display them directly without extra API hops.
- **[`src/main/java/com/memora/modules/learningpath/repository/LearningPathItemRepository.java`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/src/main/java/com/memora/modules/learningpath/repository/LearningPathItemRepository.java)**
  - Added repository methods for query by path ID and quiz ID: `findByLearningPathIdOrderByOrderIndexAsc` and `findByQuizId`.
- **[`src/main/java/com/memora/modules/learningpath/repository/LearningPathRepository.java`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/src/main/java/com/memora/modules/learningpath/repository/LearningPathRepository.java)**
  - Ensured active path queries return the latest active learning path for a given user.
- **[`src/main/java/com/memora/modules/learningpath/service/LearningPathService.java`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/src/main/java/com/memora/modules/learningpath/service/LearningPathService.java)** & **[`LearningPathServiceImpl.java`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/src/main/java/com/memora/modules/learningpath/service/LearningPathServiceImpl.java)**
  - Enriched DTO responses with vocabulary entity details.
  - Implemented auto-generation logic: when current path status is `COMPLETED`, `getTodayPath` creates Day N+1 with adaptive review/new-word mix.
- **[`src/main/java/com/memora/modules/learningpath/strategy/AdaptiveLearningPathStrategy.java`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/src/main/java/com/memora/modules/learningpath/strategy/AdaptiveLearningPathStrategy.java)**
  - Balanced item distribution between spaced repetition review items and newly introduced CEFR-level vocabulary words.
- **[`frontend/src/api/learningPathApi.ts`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/frontend/src/api/learningPathApi.ts)** & **[`frontend/src/types/learningPath.ts`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/frontend/src/types/learningPath.ts)**
  - Updated API clients and TypeScript interfaces with complete item metadata.

---

### 6. Quiz Module & Retention Synchronization
- **[`src/main/java/com/memora/modules/quiz/service/QuizServiceImpl.java`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/src/main/java/com/memora/modules/quiz/service/QuizServiceImpl.java)**
  - **Lazy Initialization Fix**: Replaced access to `LearningPath.getLearningPathItems()` (a lazy collection that failed across service transactions) with explicit `learningPathItemRepository.findByLearningPathIdOrderByOrderIndexAsc`.
  - **Learning Path Item Completion**: Automatically marks the corresponding `QUIZ` item as `COMPLETED` upon quiz completion.
  - **Path Completion Trigger**: Sets `LearningPath.status = COMPLETED`, enabling auto-progression to the next day's path.
  - **Error Logging**: Added detailed stack trace logging to catch any future sync issues gracefully without failing the quiz attempt.
- **[`src/main/java/com/memora/modules/quiz/dto/AnswerSubmissionRequest.java`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/src/main/java/com/memora/modules/quiz/dto/AnswerSubmissionRequest.java)**
  - Added support for flexible answer option format and validation.
- **[`src/main/java/com/memora/modules/quiz/repository/QuizAttemptRepository.java`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/src/main/java/com/memora/modules/quiz/repository/QuizAttemptRepository.java)**
  - Added query methods to retrieve the active attempt deterministically.
- **[`frontend/src/pages/QuizPage.tsx`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/frontend/src/pages/QuizPage.tsx)**
  - **Dynamic XP Computation**:
    $$\text{Earned XP} = (\text{Correct Answers} \times 10) + (\text{Score} == 100\% \ ? \ 20 : 0)$$
  - Updated both the completion toast notification and the final score result card to display the dynamic XP rather than a hardcoded 40 XP.

---

### 7. Security & Environment Configuration
- **[`src/main/java/com/memora/security/SecurityConfig.java`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/src/main/java/com/memora/security/SecurityConfig.java)**
  - Configured frame options (`headers.frameOptions.disable()`) for H2 console access in dev mode and verified stateless JWT filters.
- **[`src/main/resources/application-dev.yml`](file:///e:/University/UIU/11th%20Trimester/Advanced%20Object%20Oriented%20Programming%20Laboratory/Project/Memora/src/main/resources/application-dev.yml)**
  - Set up H2 in-memory PostgreSQL compatibility mode (`jdbc:h2:mem:memoradb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL`) for rapid local development and testing.

---

## 📁 Complete File Modification Matrix

| # | File Path | Component | Type | Summary of Change |
| :-: | :--- | :--- | :---: | :--- |
| 1 | `src/main/java/.../assessment/repository/AssessmentRepository.java` | Backend | Fix | Deterministic top-1 assessment query to prevent `NonUniqueResultException` |
| 2 | `src/main/java/.../gamification/dto/LeaderboardEntryResponse.java` | Backend | Refactor | Added serialization fields for rank, username, XP, streak |
| 3 | `src/main/java/.../gamification/dto/XpTransactionResponse.java` | Backend | Refactor | Added activity type and XP audit fields |
| 4 | `src/main/java/.../learningpath/controller/LearningPathController.java` | Backend | Feature | Enriched path item endpoints with word details |
| 5 | `src/main/java/.../learningpath/dto/LearningPathItemResponse.java` | Backend | Feature | Added definition, example, CEFR level, and pronunciation |
| 6 | `src/main/java/.../learningpath/repository/LearningPathItemRepository.java` | Backend | Feature | Added `findByLearningPathIdOrderByOrderIndexAsc` and `findByQuizId` |
| 7 | `src/main/java/.../learningpath/repository/LearningPathRepository.java` | Backend | Fix | Deterministic active path retrieval for user |
| 8 | `src/main/java/.../learningpath/service/LearningPathService.java` | Backend | Feature | Interface updates for path item and completion status |
| 9 | `src/main/java/.../learningpath/service/LearningPathServiceImpl.java` | Backend | Feature | Item mapping, auto-advancement on completion |
| 10 | `src/main/java/.../learningpath/strategy/AdaptiveLearningPathStrategy.java` | Backend | Refactor | Balanced daily ratio of spaced reviews vs new words |
| 11 | `src/main/java/.../quiz/dto/AnswerSubmissionRequest.java` | Backend | Refactor | Flexible payload parsing for option selections |
| 12 | `src/main/java/.../quiz/repository/QuizAttemptRepository.java` | Backend | Fix | Deterministic active attempt lookup |
| 13 | `src/main/java/.../quiz/service/QuizServiceImpl.java` | Backend | Fix | Fixed lazy loading exception & synced quiz completion with learning path |
| 14 | `src/main/java/.../security/SecurityConfig.java` | Backend | Config | Enabled H2 console frame options in dev |
| 15 | `src/main/resources/application-dev.yml` | Backend | Config | Development database configuration |
| 16 | `frontend/src/api/learningPathApi.ts` | Frontend | Feature | Typed API endpoints for enriched learning path |
| 17 | `frontend/src/components/layout/Sidebar.tsx` | Frontend | UI/UX | Fixed sidebar height (`h-screen`), removed white gap, anchored profile |
| 18 | `frontend/src/components/ui/Modal.tsx` | Frontend | UI/UX | Removed blurry background filter, centered modal, refined styling |
| 19 | `frontend/src/pages/AssessmentResultPage.tsx` | Frontend | Fix | Null safety on placement tier and auto-redirect |
| 20 | `frontend/src/pages/LeaderboardPage.tsx` | Frontend | Fix | Resolved white screen with null-safe leaderboard render |
| 21 | `frontend/src/pages/LearningPathPage.tsx` | Frontend | UI/UX / Feature | Interactive word study modal, +15 XP alignment, auto-refresh on finish |
| 22 | `frontend/src/pages/ProfilePage.tsx` | Frontend | Fix | Resolved white screen with defensive null checks on user profile stats |
| 23 | `frontend/src/pages/QuizPage.tsx` | Frontend | Feature / Fix | Dynamic XP toast & results calculation matching backend `QuizRewardStrategy` |
| 24 | `frontend/src/types/gamification.ts` | Frontend | Types | Added null-safe fields to Leaderboard and Profile types |
| 25 | `frontend/src/types/learningPath.ts` | Frontend | Types | Extended type definitions for vocabulary metadata in items |

---

## 🔍 Verification & Testing Summary

1. **Backend Compilation & Health Check**:
   - Clean compiled with `./mvnw.cmd clean compile -DskipTests` (0 errors).
   - Backend started with Spring Boot profile `dev` on port 8080.
   - Endpoint `GET /api/v1/health` responded with `{"status":"UP","service":"Memora Backend Platform"}`.
2. **Frontend Type-Checking & Build**:
   - Ran `npx tsc --noEmit` with **0 TypeScript errors**.
   - Vite development server running on `http://localhost:5173`.
3. **End-to-End User Journeys Verified**:
   - Navigating to `/leaderboard` and `/profile` displays content properly without white screens.
   - Sidebar displays in fixed height with no empty gaps.
   - Clicking a vocabulary card opens the study modal with definitions and examples.
   - Completing a word displays **+15 XP**.
   - Finishing the Daily Retention Quiz marks the item complete and allows the curriculum to advance to the next day's learning path.

package com.memora.modules.partner.challenge.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.partner.challenge.domain.ChallengeStatus;
import com.memora.modules.partner.entity.PartnerRelationship;
import com.memora.modules.quiz.entity.Quiz;
import com.memora.modules.user.entity.User;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Domain entity representing a competitive vocabulary challenge between two accepted learning partners.
 * Encapsulates the shared deterministic question set (via {@link Quiz}) while isolating each participant's scores.
 */
@Entity
@Table(name = "vocabulary_challenges",
        indexes = {
                @Index(name = "idx_vocab_challenge_rel", columnList = "relationship_id"),
                @Index(name = "idx_vocab_challenge_challenger", columnList = "challenger_id"),
                @Index(name = "idx_vocab_challenge_challenged", columnList = "challenged_user_id"),
                @Index(name = "idx_vocab_challenge_status", columnList = "status")
        })
public class VocabularyChallenge extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "relationship_id", nullable = false)
    private PartnerRelationship relationship;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenger_id", nullable = false)
    private User challenger;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenged_user_id", nullable = false)
    private User challengedUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChallengeStatus status = ChallengeStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "cefr_level", nullable = false)
    private DifficultyLevel cefrLevel;

    @Column(name = "question_count", nullable = false)
    private int questionCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(name = "challenger_score")
    private Integer challengerScore;

    @Column(name = "challenged_score")
    private Integer challengedScore;

    @Column(name = "challenger_completed_at")
    private Instant challengerCompletedAt;

    @Column(name = "challenged_completed_at")
    private Instant challengedCompletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    public VocabularyChallenge() {
    }

    public VocabularyChallenge(PartnerRelationship relationship,
                               User challenger,
                               User challengedUser,
                               DifficultyLevel cefrLevel,
                               int questionCount,
                               Quiz quiz) {
        this.relationship = relationship;
        this.challenger = challenger;
        this.challengedUser = challengedUser;
        this.cefrLevel = cefrLevel;
        this.questionCount = questionCount;
        this.quiz = quiz;
        this.status = ChallengeStatus.PENDING;
    }

    public PartnerRelationship getRelationship() {
        return relationship;
    }

    public void setRelationship(PartnerRelationship relationship) {
        this.relationship = relationship;
    }

    public User getChallenger() {
        return challenger;
    }

    public void setChallenger(User challenger) {
        this.challenger = challenger;
    }

    public User getChallengedUser() {
        return challengedUser;
    }

    public void setChallengedUser(User challengedUser) {
        this.challengedUser = challengedUser;
    }

    public ChallengeStatus getStatus() {
        return status;
    }

    public void setStatus(ChallengeStatus status) {
        this.status = status;
    }

    public DifficultyLevel getCefrLevel() {
        return cefrLevel;
    }

    public void setCefrLevel(DifficultyLevel cefrLevel) {
        this.cefrLevel = cefrLevel;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Integer getChallengerScore() {
        return challengerScore;
    }

    public void setChallengerScore(Integer challengerScore) {
        this.challengerScore = challengerScore;
    }

    public Integer getChallengedScore() {
        return challengedScore;
    }

    public void setChallengedScore(Integer challengedScore) {
        this.challengedScore = challengedScore;
    }

    public Instant getChallengerCompletedAt() {
        return challengerCompletedAt;
    }

    public void setChallengerCompletedAt(Instant challengerCompletedAt) {
        this.challengerCompletedAt = challengerCompletedAt;
    }

    public Instant getChallengedCompletedAt() {
        return challengedCompletedAt;
    }

    public void setChallengedCompletedAt(Instant challengedCompletedAt) {
        this.challengedCompletedAt = challengedCompletedAt;
    }

    public User getWinner() {
        return winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }

    public boolean isDraw() {
        return challengerCompletedAt != null && challengedCompletedAt != null && winner == null;
    }
}

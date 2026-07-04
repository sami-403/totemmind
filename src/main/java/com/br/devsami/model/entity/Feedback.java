package com.br.devsami.model.entity;

import com.br.devsami.model.enums.FeedbackCategory;
import com.br.devsami.model.enums.Feelling;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feedbacks")

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Feelling feelling;

    @Enumerated(EnumType.STRING)
    private FeedbackCategory category;

    private Integer confidence; // coisas futuras
    private Boolean sarcasmDetected; // coisas futuras
    private String reasoning; // coisas futuras
    private Float score; // coisas futuras

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * @return UUID return the id
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return String return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return Feelling return the feelling
     */
    public Feelling getFeelling() {
        return feelling;
    }

    /**
     * @param feelling the feelling to set
     */
    public void setFeelling(Feelling feelling) {
        this.feelling = feelling;
    }

    /**
     * @return FeedbackCategory return the category
     */
    public FeedbackCategory getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(FeedbackCategory category) {
        this.category = category;
    }

    /**
     * @return User return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return Employee return the employee
     */
    public Employee getEmployee() {
        return employee;
    }

    /**
     * @param employee the employee to set
     */
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    /**
     * @return LocalDateTime return the createdAt
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @return LocalDateTime return the updatedAt
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @return Integer return the confidence
     */
    public Integer getConfidence() {
        return confidence;
    }

    /**
     * @param confidence the confidence to set
     */
    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    /**
     * @return Boolean return the sarcasmDetected
     */
    public Boolean getSarcasmDetected() {
        return sarcasmDetected;
    }

    /**
     * @param sarcasmDetected the sarcasmDetected to set
     */
    public void setSarcasmDetected(Boolean sarcasmDetected) {
        this.sarcasmDetected = sarcasmDetected;
    }

    /**
     * @return String return the reasoning
     */
    public String getReasoning() {
        return reasoning;
    }

    /**
     * @param reasoning the reasoning to set
     */
    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    /**
     * @return Float return the score
     */
    public Float getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(Float score) {
        this.score = score;
    }
}
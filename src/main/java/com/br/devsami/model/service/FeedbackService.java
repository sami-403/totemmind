package com.br.devsami.model.service;

import com.br.devsami.model.entity.Employee;
import com.br.devsami.model.entity.Feedback;
import com.br.devsami.model.entity.User;
import com.br.devsami.model.repository.FeedbackRepository;
import com.br.devsami.utils.enums.FeedbackCategory;
import com.br.devsami.utils.enums.Feelling;

public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackService() {
        this.feedbackRepository = new FeedbackRepository();
    }

    public Feedback createFeedback(
            User user,
            Employee employee,
            Feelling feeling,
            FeedbackCategory category,
            String text) {

        if (user == null) {
            throw new IllegalArgumentException("User obrigatório");
        }

        if (employee == null) {
            throw new IllegalArgumentException("Employee obrigatório");
        }

        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setEmployee(employee);
        feedback.setFeelling(feeling);
        feedback.setCategory(category);
        feedback.setText(text);

        // regras simples iniciais
        if (text == null || text.isBlank()) {
            feedback.setConfidence(100);
            feedback.setSarcasmDetected(false);
            feedback.setReasoning("Feedback sem texto (voto direto)");
        }

        feedbackRepository.save(feedback);

        return feedback;
    }
}
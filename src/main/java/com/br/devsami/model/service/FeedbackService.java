package com.br.devsami.model.service;

import com.br.devsami.model.ai.AiOrchestratorService;
import com.br.devsami.model.entity.Employee;
import com.br.devsami.model.entity.Feedback;
import com.br.devsami.model.entity.User;
import com.br.devsami.model.repository.FeedbackRepository;
import com.br.devsami.utils.enums.FeedbackCategory;
import com.br.devsami.utils.enums.Feelling;

public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final AiOrchestratorService aiService;

    public FeedbackService() {
        this.feedbackRepository = new FeedbackRepository();
        this.aiService = new AiOrchestratorService();
    }

    public Feedback createFeedback(
            User user,
            Employee employee,
            Feelling feeling,
            FeedbackCategory category,
            String text) {

        if (user == null || employee == null) {
            throw new IllegalArgumentException("User e Employee são obrigatórios");
        }

        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setEmployee(employee);
        feedback.setCategory(category);
        feedback.setText(text);

        if (text == null || text.isBlank()) {
            feedback.setFeelling(feeling);
            feedback.setConfidence(100);
            feedback.setSarcasmDetected(false);
            feedback.setReasoning("Voto direto sem texto");
        } else {
            try {
                String inputIa = "Original Feeling: " + feeling.name() + "\nText: " + text;
                String iaResponse = aiService.processMessage(inputIa).trim();

                feedback.setFeelling(Feelling.valueOf(iaResponse));
                feedback.setReasoning("Analisado via IA");
            } catch (Exception e) {
                feedback.setFeelling(feeling);
                feedback.setReasoning("Fallback: Erro na IA. Voto original mantido.");
            }
        }

        feedbackRepository.save(feedback);
        return feedback;
    }
}
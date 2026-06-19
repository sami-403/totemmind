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

        var feedback = new Feedback(); // var infere o tipo, não precisa repetir o "Feedback".
        feedback.setUser(user);
        feedback.setEmployee(employee);
        feedback.setCategory(category);
        feedback.setText(text);

        // se não há texto, assume o sentimento do botão clicado para economizar processamento
        if (text == null || text.isBlank()) {
            feedback.setFeelling(feeling);
            feedback.setConfidence(100);
            feedback.setSarcasmDetected(false);
            feedback.setReasoning("Voto direto sem texto");
        } else {
            try {
                // Tipa direto para o enum para melhorar a velocidade e leitura
                Feelling trueSentiment = aiService.classificarSentimento(feeling, text);

                feedback.setFeelling(trueSentiment);
                feedback.setReasoning("Analisado via IA");
            } catch (Exception e) {
                // Fallback seguro caso a IA caia (Ollama fora do ar, etc)
                feedback.setFeelling(feeling);
                feedback.setReasoning("Fallback: Erro de conexão com a IA.");
                System.out.println("❌ ERRO REAL DA IA: " + e.getMessage());
                e.printStackTrace();
            }
        }

        feedbackRepository.save(feedback);
        return feedback;
    }
}
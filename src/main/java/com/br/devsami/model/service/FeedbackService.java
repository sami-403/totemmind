package com.br.devsami.model.service;

import com.br.devsami.ai.AiOrchestratorService;
import com.br.devsami.model.entity.Employee;
import com.br.devsami.model.entity.Feedback;
import com.br.devsami.model.entity.EmployeeFeedback;
import com.br.devsami.model.entity.Product;
import com.br.devsami.model.entity.ProductFeedback;
import com.br.devsami.model.entity.User;
import com.br.devsami.model.repository.FeedbackRepository;
import com.br.devsami.model.enums.FeedbackCategory;
import com.br.devsami.model.enums.Feeling;

public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final AiOrchestratorService aiService;

    public FeedbackService() {
        this.feedbackRepository = new FeedbackRepository();
        this.aiService = new AiOrchestratorService();
    }

    // Cria feedback de funcionário
    public EmployeeFeedback createEmployeeFeedback(
            User user,
            Employee employee,
            Feeling feeling,
            FeedbackCategory category,
            String text) {

        if (user == null || employee == null) {
            throw new IllegalArgumentException("User e Employee são obrigatórios");
        }

        var feedback = new EmployeeFeedback();
        feedback.setUser(user);
        feedback.setEmployee(employee);
        feedback.setCategory(category);
        feedback.setText(text);

        classifyFeeling((Feedback) feedback, feeling, text);

        if (text != null && !text.isBlank()) {
            try {
                var empCategory = aiService.inferirCategoriaAtendimento(feedback.getFeeling(), text);
                feedback.setEmployeeCategory(empCategory);
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao inferir categoria de atendimento: " + e.getMessage());
            }
        } else {
            feedback.setEmployeeCategory(com.br.devsami.model.enums.EmployeeFeedbackCategory.OTHER);
        }

        feedbackRepository.save(feedback);
        return feedback;
    }

    public ProductFeedback createProductFeedback(
            User user,
            Product product,
            Integer rating,
            FeedbackCategory category,
            String text) {

        if (user == null || product == null) {
            throw new IllegalArgumentException("User e Product são obrigatórios");
        }

        var feedback = new ProductFeedback();
        feedback.setUser(user);
        feedback.setProduct(product);
        feedback.setRating(rating);
        feedback.setCategory(category);
        feedback.setText(text);

        if (text != null && !text.isBlank()) {
            classifyFeeling((Feedback) feedback, Feeling.NEUTRAL, text);
            try {
                var prodCategory = aiService.inferirCategoriaProduto(rating, text);
                feedback.setProductCategory(prodCategory);
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao inferir categoria de produto: " + e.getMessage());
            }
        } else {
            feedback.setReasoning("Voto direto por estrelas sem texto");
            feedback.setProductCategory(com.br.devsami.model.enums.ProductFeedbackCategory.OTHER);
        }

        feedbackRepository.save(feedback);
        return feedback;
    }

    public ProductFeedback createProductFeedback(
            User user,
            Product product,
            Feeling feeling,
            FeedbackCategory category,
            String text) {
        return createProductFeedback(user, product, (Integer) null, category, text);
    }

    private void classifyFeeling(Feedback feedback, Feeling feeling, String text) {
        // se não há texto, assume o sentimento do botão clicado para economizar processamento
        if (text == null || text.isBlank()) {
            feedback.setFeeling(feeling);
            feedback.setConfidence(100);
            feedback.setSarcasmDetected(false);
            feedback.setReasoning("Voto direto sem texto");
        } else {
            try {
                // Tipa direto para o enum para melhorar a velocidade e leitura
                Feeling trueSentiment = aiService.classificarSentimento(feeling, text);

                feedback.setFeeling(trueSentiment);
                feedback.setReasoning("Analisado via IA");
            } catch (Exception e) {
                // Fallback seguro caso a IA caia (Ollama fora do ar, etc)
                feedback.setFeeling(feeling);
                feedback.setReasoning("Fallback: Erro de conexão com a IA.");
                System.out.println("❌ ERRO REAL DA IA: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void createFeedback(Feedback feedback) {
        if (feedback == null) {
            throw new IllegalArgumentException("Feedback não pode ser nulo");
        }
        feedbackRepository.save(feedback);
    }

    public void updateFeedback(Feedback feedback) {
        if (feedback == null) {
            throw new IllegalArgumentException("Feedback não pode ser nulo");
        }
        feedbackRepository.update(feedback);
    }

    public void deleteFeedback(Feedback feedback) {
        if (feedback == null) {
            throw new IllegalArgumentException("Feedback não pode ser nulo");
        }
        feedbackRepository.delete(feedback);
    }

    public java.util.List<Feedback> findAllFeedbacks() {
        return feedbackRepository.findAll();
    }
}

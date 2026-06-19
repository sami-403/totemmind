package com.br.devsami.model.service;

import com.br.devsami.model.entity.Feedback;
import com.br.devsami.model.repository.FeedbackRepository;
import com.br.devsami.utils.enums.Feelling;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class FeedbackAnalyticsService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackAnalyticsService() {
        this.feedbackRepository = new FeedbackRepository();
    }

    // Calcula a porcentagem de cada sentimento no formato: [Satisfeito, Neutro, Insatisfeito]
    public double[] calcularPercentagens(Long employeeId, LocalDateTime start, LocalDateTime end) {
        List<Feedback> feedbacks;

        // Filtra pelo período apenas se ambas as datas forem informadas
        if (start != null && end != null) {
            feedbacks = feedbackRepository.findByEmployeeAndPeriod(employeeId, start, end);
        } else {
            feedbacks = feedbackRepository.findByEmployeeId(employeeId);
        }

        // Previne ArithmeticException (divisão por zero)
        if (feedbacks.isEmpty()) {
            return new double[]{0.0, 0.0, 0.0};
        }

        int satisfeitos = 0, neutros = 0, insatisfeitos = 0;

        for (Feedback feedback : feedbacks) {
            if (feedback.getFeelling() == Feelling.SATISFIED) satisfeitos++;
            else if (feedback.getFeelling() == Feelling.NEUTRAL) neutros++;
            else if (feedback.getFeelling() == Feelling.DISSATISFIED) insatisfeitos++;
        }

        int total = feedbacks.size();

        double pctSatisfied = ((double) satisfeitos / total) * 100;
        double pctNeutral = ((double) neutros / total) * 100;
        double pctDissatisfied = ((double) insatisfeitos / total) * 100;

        return new double[]{pctSatisfied, pctNeutral, pctDissatisfied};
    }

    // Retorna a data do feedback mais antigo do funcionário
    public LocalDate buscarPrimeiraData(Long employeeId) {
        return feedbackRepository.findByEmployeeId(employeeId).stream()
                .map(Feedback::getCreatedAt)
                .min(LocalDateTime::compareTo)
                .map(LocalDateTime::toLocalDate)
                .orElse(null);
    }

    // Retorna a data do feedback mais recente do funcionário
    public LocalDate buscarUltimaData(Long employeeId) {
        return feedbackRepository.findByEmployeeId(employeeId).stream()
                .map(Feedback::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .map(LocalDateTime::toLocalDate)
                .orElse(null);
    }
}
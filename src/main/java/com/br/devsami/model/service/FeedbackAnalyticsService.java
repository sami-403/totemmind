package com.br.devsami.model.service;

import com.br.devsami.model.entity.Employee;
import com.br.devsami.model.entity.Feedback;
import com.br.devsami.model.repository.FeedbackRepository;
import com.br.devsami.model.enums.Feeling;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class FeedbackAnalyticsService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackAnalyticsService() {
        this.feedbackRepository = new FeedbackRepository();
    }

    public double[] calcularPercentagens(Long employeeId, LocalDateTime start, LocalDateTime end) {
        List<Feedback> feedbacks;

        if (start != null && end != null) {
            feedbacks = feedbackRepository.findByEmployeeAndPeriod(employeeId, start, end);
        } else {
            feedbacks = feedbackRepository.findByEmployeeId(employeeId);
        }

        if (feedbacks.isEmpty()) {
            return new double[] { 0.0, 0.0, 0.0 };
        }

        int satisfeitos = 0, neutros = 0, insatisfeitos = 0;

        for (Feedback feedback : feedbacks) {
            if (feedback.getFeeling() == Feeling.SATISFIED)
                satisfeitos++;
            else if (feedback.getFeeling() == Feeling.NEUTRAL)
                neutros++;
            else if (feedback.getFeeling() == Feeling.DISSATISFIED)
                insatisfeitos++;
        }

        int total = feedbacks.size();

        double pctSatisfied = Math.round(((double) satisfeitos / total) * 10000.0) / 100.0;
        double pctNeutral = Math.round(((double) neutros / total) * 10000.0) / 100.0;
        double pctDissatisfied = Math.round(((double) insatisfeitos / total) * 10000.0) / 100.0;

        return new double[] { pctSatisfied, pctNeutral, pctDissatisfied };
    }

    public LocalDate buscarPrimeiraData(Long employeeId) {
        return feedbackRepository.findByEmployeeId(employeeId).stream()
                .map(Feedback::getCreatedAt)
                .min(LocalDateTime::compareTo)
                .map(LocalDateTime::toLocalDate)
                .orElse(null);
    }

    public LocalDate buscarUltimaData(Long employeeId) {
        return feedbackRepository.findByEmployeeId(employeeId).stream()
                .map(Feedback::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .map(LocalDateTime::toLocalDate)
                .orElse(null);
    }

    public String obterMaiorTaxa(List<Employee> funcionarios, int indice, LocalDateTime start, LocalDateTime end) {
        Employee campeao = null;
        double maiorTaxa = 0.0; // Mude de -1 para 0.0

        for (Employee e : funcionarios) {
            double[] taxas = calcularPercentagens(e.getId(), start, end);
            if (taxas[indice] > maiorTaxa) {
                maiorTaxa = taxas[indice];
                campeao = e;
            }
        }

        String[] nomesSentimentos = { "Satisfação", "Neutralidade", "Insatisfação" };

        // Se campeao continuar nulo, significa que ninguém teve taxa maior que 0%
        return campeao != null
                ? String.format("%s com %.2f%% de %s", campeao.getName(), maiorTaxa, nomesSentimentos[indice])
                : "[SEM_DADOS]";
    }
}
package com.br.devsami.model.service;

import com.br.devsami.model.entity.Feedback;
import com.br.devsami.model.repository.FeedbackRepository;
import com.br.devsami.utils.enums.Feelling;

import java.time.LocalDateTime;
import java.util.List;

public class FeedbackAnalyticsService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackAnalyticsService() {
        this.feedbackRepository = new FeedbackRepository();
    }

    /**
     * Calcula as porcentagens dos sentimentos associados a um funcionário.
     * Retorna um vetor numérico: [0] = Satisfeito, [1] = Neutro, [2] = Insatisfeito
     */
    public double[] calcularPercentagens(Long employeeId, LocalDateTime start, LocalDateTime end) {
        List<Feedback> feedbacks;

        // Se o utilizador especificar datas, faz a pesquisa por período. Caso contrário, devolve tudo.
        if (start != null && end != null) {
            feedbacks = feedbackRepository.findByEmployeeAndPeriod(employeeId, start, end);
        } else {
            feedbacks = feedbackRepository.findByEmployeeId(employeeId);
        }

        // Array zerado para tornar o comportamento previsivel em caso de falha
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

        // O casting para (double) assegura as casas decimais na divisão
        double pctSatisfied = ((double) satisfeitos / total) * 100;
        double pctNeutral = ((double) neutros / total) * 100;
        double pctDissatisfied = ((double) insatisfeitos / total) * 100;

        return new double[]{pctSatisfied, pctNeutral, pctDissatisfied};
    }
}
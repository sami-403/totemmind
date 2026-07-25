package com.br.devsami.model.service;

import com.br.devsami.model.entity.Employee;
import com.br.devsami.model.entity.Feedback;
import com.br.devsami.model.entity.EmployeeFeedback;
import com.br.devsami.model.entity.Product;
import com.br.devsami.model.entity.ProductFeedback;
import com.br.devsami.model.enums.Feeling;
import com.br.devsami.model.enums.ProductFeedbackCategory;
import com.br.devsami.model.repository.FeedbackRepository;
import com.br.devsami.model.repository.ProductRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class FeedbackAnalyticsService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackAnalyticsService() {
        this.feedbackRepository = new FeedbackRepository();
    }

    public double[] calcularPercentagens(Long employeeId, LocalDateTime start, LocalDateTime end) {
        List<EmployeeFeedback> feedbacks;

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
        if (indice < 0 || indice > 2) {
            indice = 0;
        }
        Employee campeao = null;
        double maiorTaxa = 0.0;

        for (Employee e : funcionarios) {
            double[] taxas = calcularPercentagens(e.getId(), start, end);
            if (taxas[indice] > maiorTaxa) {
                maiorTaxa = taxas[indice];
                campeao = e;
            }
        }

        String[] nomesSentimentos = { "Satisfação", "Neutralidade", "Insatisfação" };

        return campeao != null
                ? String.format("%s com %.2f%% de %s", campeao.getName(), maiorTaxa, nomesSentimentos[indice])
                : "[SEM_DADOS]";
    }

    public Map<String, int[]> evolucaoSatisfacao(Long employeeId, LocalDateTime start, LocalDateTime end) {
        List<EmployeeFeedback> feedbacks = (start != null && end != null)
                ? feedbackRepository.findByEmployeeAndPeriod(employeeId, start, end)
                : feedbackRepository.findByEmployeeId(employeeId);

        Map<String, int[]> evolucao = new TreeMap<>();

        for (Feedback f : feedbacks) {
            String data = f.getCreatedAt().toLocalDate().toString();
            evolucao.putIfAbsent(data, new int[]{0, 0, 0});

            if (f.getFeeling() == Feeling.SATISFIED) evolucao.get(data)[0]++;
            else if (f.getFeeling() == Feeling.NEUTRAL) evolucao.get(data)[1]++;
            else if (f.getFeeling() == Feeling.DISSATISFIED) evolucao.get(data)[2]++;
        }
        return evolucao;
    }

    public double obterMediaEstrelasProduto(UUID productId) {
        Double media = feedbackRepository.findAverageRatingByProduct(productId);
        return Math.round(media * 100.0) / 100.0;
    }

    public String obterProdutosPorFaixaDeNota(double minRating, double maxRating, LocalDateTime start, LocalDateTime end) {
        ProductRepository productRepo = new ProductRepository();
        List<Product> produtos = productRepo.findAll();
        if (produtos.isEmpty()) {
            return "Nenhum produto cadastrado no sistema.";
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (Product p : produtos) {
            List<ProductFeedback> feedbacks = feedbackRepository.findProductFeedbacksByProductAndPeriod(p.getId(), start, end);
            if (feedbacks.isEmpty()) continue;

            double soma = 0.0;
            int totalVotos = 0;
            for (ProductFeedback pf : feedbacks) {
                if (pf.getRating() != null) {
                    soma += pf.getRating();
                    totalVotos++;
                }
            }

            if (totalVotos > 0) {
                double media = Math.round((soma / totalVotos) * 100.0) / 100.0;
                if (media >= minRating && media <= maxRating) {
                    count++;
                    sb.append(String.format("- %s (ID do BD: %s): Média %.2f ⭐ (%d avaliações)\n",
                            p.getName(), p.getId().toString(), media, totalVotos));
                }
            }
        }

        if (count == 0) {
            return String.format("Nenhum produto encontrado com média de notas entre %.1f e %.1f estrelas.", minRating, maxRating);
        }

        return String.format("Produtos com média de notas entre %.1f e %.1f estrelas (%d encontrados):\n%s", minRating, maxRating, count, sb.toString());
    }

    public Map<ProductFeedbackCategory, Double> calcularDistribuicaoCategoriasProdutoGeral(LocalDateTime start, LocalDateTime end) {
        List<ProductFeedback> feedbacks = feedbackRepository.findAllProductFeedbacks(start, end);
        Map<ProductFeedbackCategory, Double> porcentagens = new HashMap<>();
        if (feedbacks.isEmpty()) {
            return porcentagens;
        }

        Map<ProductFeedbackCategory, Integer> contagem = new HashMap<>();
        for (ProductFeedbackCategory cat : ProductFeedbackCategory.values()) {
            contagem.put(cat, 0);
        }

        int total = 0;
        for (ProductFeedback pf : feedbacks) {
            if (pf.getProductCategory() != null) {
                contagem.put(pf.getProductCategory(), contagem.getOrDefault(pf.getProductCategory(), 0) + 1);
                total++;
            }
        }

        if (total > 0) {
            for (ProductFeedbackCategory cat : ProductFeedbackCategory.values()) {
                double pct = Math.round(((double) contagem.get(cat) / total) * 10000.0) / 100.0;
                porcentagens.put(cat, pct);
            }
        }

        return porcentagens;
    }

    public String calcularMediaEstrelasPorCategoriaProduto(LocalDateTime start, LocalDateTime end) {
        List<ProductFeedback> feedbacks = feedbackRepository.findAllProductFeedbacks(start, end);
        if (feedbacks.isEmpty()) {
            return "Nenhum feedback de produto registrado no período.";
        }

        Map<ProductFeedbackCategory, Double> somaNotas = new HashMap<>();
        Map<ProductFeedbackCategory, Integer> contagem = new HashMap<>();

        for (ProductFeedback pf : feedbacks) {
            if (pf.getProductCategory() != null && pf.getRating() != null) {
                somaNotas.put(pf.getProductCategory(), somaNotas.getOrDefault(pf.getProductCategory(), 0.0) + pf.getRating());
                contagem.put(pf.getProductCategory(), contagem.getOrDefault(pf.getProductCategory(), 0) + 1);
            }
        }

        StringBuilder sb = new StringBuilder("Média de notas em estrelas por categoria de produto:\n");
        for (ProductFeedbackCategory cat : ProductFeedbackCategory.values()) {
            int count = contagem.getOrDefault(cat, 0);
            if (count > 0) {
                double media = Math.round((somaNotas.get(cat) / count) * 100.0) / 100.0;
                sb.append(String.format("- %s: %.2f ⭐ (%d avaliações)\n", cat.getDescription(), media, count));
            }
        }
        return sb.toString();
    }

    public Map<ProductFeedbackCategory, Double> calcularDistribuicaoCategoriasProdutoIndividual(UUID productId, LocalDateTime start, LocalDateTime end) {
        List<ProductFeedback> feedbacks = feedbackRepository.findProductFeedbacksByProductAndPeriod(productId, start, end);
        Map<ProductFeedbackCategory, Double> porcentagens = new HashMap<>();
        if (feedbacks.isEmpty()) {
            return porcentagens;
        }

        Map<ProductFeedbackCategory, Integer> contagem = new HashMap<>();
        for (ProductFeedbackCategory cat : ProductFeedbackCategory.values()) {
            contagem.put(cat, 0);
        }

        int total = 0;
        for (ProductFeedback pf : feedbacks) {
            if (pf.getProductCategory() != null) {
                contagem.put(pf.getProductCategory(), contagem.getOrDefault(pf.getProductCategory(), 0) + 1);
                total++;
            }
        }

        if (total > 0) {
            for (ProductFeedbackCategory cat : ProductFeedbackCategory.values()) {
                double pct = Math.round(((double) contagem.get(cat) / total) * 10000.0) / 100.0;
                porcentagens.put(cat, pct);
            }
        }

        return porcentagens;
    }

    public double[] calcularDistribuicaoEstrelasProduto(UUID productId, LocalDateTime start, LocalDateTime end) {
        List<ProductFeedback> feedbacks = feedbackRepository.findProductFeedbacksByProductAndPeriod(productId, start, end);
        if (feedbacks.isEmpty()) {
            return new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
        }

        int[] contagem = new int[5]; // index 0=5 estrelas, 1=4 estrelas, 2=3 estrelas, 3=2 estrelas, 4=1 estrela
        int total = 0;

        for (ProductFeedback pf : feedbacks) {
            if (pf.getRating() != null && pf.getRating() >= 1 && pf.getRating() <= 5) {
                int starIdx = 5 - pf.getRating();
                contagem[starIdx]++;
                total++;
            }
        }

        if (total == 0) {
            return new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
        }

        double[] porcentagens = new double[5];
        for (int i = 0; i < 5; i++) {
            porcentagens[i] = Math.round(((double) contagem[i] / total) * 10000.0) / 100.0;
        }

        return porcentagens;
    }

    public Map<com.br.devsami.model.enums.EmployeeFeedbackCategory, Double> calcularDistribuicaoCategoriasAtendimentoGeral(LocalDateTime start, LocalDateTime end) {
        List<EmployeeFeedback> feedbacks = (start != null && end != null)
                ? feedbackRepository.findAllEmployeeFeedbacksByPeriod(start, end)
                : feedbackRepository.findAllEmployeeFeedbacks();

        Map<com.br.devsami.model.enums.EmployeeFeedbackCategory, Double> porcentagens = new HashMap<>();
        if (feedbacks.isEmpty()) {
            return porcentagens;
        }

        Map<com.br.devsami.model.enums.EmployeeFeedbackCategory, Integer> contagem = new HashMap<>();
        for (com.br.devsami.model.enums.EmployeeFeedbackCategory cat : com.br.devsami.model.enums.EmployeeFeedbackCategory.values()) {
            contagem.put(cat, 0);
        }

        int total = 0;
        for (EmployeeFeedback ef : feedbacks) {
            if (ef.getEmployeeCategory() != null) {
                contagem.put(ef.getEmployeeCategory(), contagem.getOrDefault(ef.getEmployeeCategory(), 0) + 1);
                total++;
            }
        }

        if (total > 0) {
            for (com.br.devsami.model.enums.EmployeeFeedbackCategory cat : com.br.devsami.model.enums.EmployeeFeedbackCategory.values()) {
                double pct = Math.round(((double) contagem.get(cat) / total) * 10000.0) / 100.0;
                porcentagens.put(cat, pct);
            }
        }

        return porcentagens;
    }

    public Map<com.br.devsami.model.enums.EmployeeFeedbackCategory, Double> calcularDistribuicaoCategoriasAtendimentoIndividual(Long employeeId, LocalDateTime start, LocalDateTime end) {
        List<EmployeeFeedback> feedbacks = (start != null && end != null)
                ? feedbackRepository.findByEmployeeAndPeriod(employeeId, start, end)
                : feedbackRepository.findByEmployeeId(employeeId);

        Map<com.br.devsami.model.enums.EmployeeFeedbackCategory, Double> porcentagens = new HashMap<>();
        if (feedbacks.isEmpty()) {
            return porcentagens;
        }

        Map<com.br.devsami.model.enums.EmployeeFeedbackCategory, Integer> contagem = new HashMap<>();
        for (com.br.devsami.model.enums.EmployeeFeedbackCategory cat : com.br.devsami.model.enums.EmployeeFeedbackCategory.values()) {
            contagem.put(cat, 0);
        }

        int total = 0;
        for (EmployeeFeedback ef : feedbacks) {
            if (ef.getEmployeeCategory() != null) {
                contagem.put(ef.getEmployeeCategory(), contagem.getOrDefault(ef.getEmployeeCategory(), 0) + 1);
                total++;
            }
        }

        if (total > 0) {
            for (com.br.devsami.model.enums.EmployeeFeedbackCategory cat : com.br.devsami.model.enums.EmployeeFeedbackCategory.values()) {
                double pct = Math.round(((double) contagem.get(cat) / total) * 10000.0) / 100.0;
                porcentagens.put(cat, pct);
            }
        }

        return porcentagens;
    }

    public String obterFuncionariosPorFaixaDeSatisfacao(double minPercent, double maxPercent, LocalDateTime start, LocalDateTime end) {
        com.br.devsami.model.repository.EmployeeRepository employeeRepo = new com.br.devsami.model.repository.EmployeeRepository();
        List<Employee> todos = employeeRepo.findAllActive();
        if (todos.isEmpty()) {
            return "Nenhum funcionário ativo encontrado.";
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (Employee e : todos) {
            double[] taxas = calcularPercentagens(e.getId(), start, end);
            double sat = taxas[0]; // 0 = Satisfação
            if (sat >= minPercent && sat <= maxPercent) {
                count++;
                List<EmployeeFeedback> fbs = (start != null && end != null)
                        ? feedbackRepository.findByEmployeeAndPeriod(e.getId(), start, end)
                        : feedbackRepository.findByEmployeeId(e.getId());
                sb.append(String.format("- %s (ID do BD: %d - %s): %.2f%% de Satisfação (%d atendimentos)\n",
                        e.getName(), e.getId(), e.getTipo(), sat, fbs.size()));
            }
        }

        if (count == 0) {
            return String.format("Nenhum funcionário encontrado com taxa de satisfação entre %.1f%% e %.1f%%.", minPercent, maxPercent);
        }

        return String.format("Funcionários com taxa de satisfação entre %.1f%% e %.1f%% (%d encontrados):\n%s", minPercent, maxPercent, count, sb.toString());
    }
}

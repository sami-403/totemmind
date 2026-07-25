package com.br.devsami.ai;

import com.br.devsami.infrastructure.charts.ChartManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import com.br.devsami.model.repository.EmployeeRepository;
import com.br.devsami.model.entity.Employee;
import com.br.devsami.model.service.FeedbackAnalyticsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import com.br.devsami.model.entity.Product;
import com.br.devsami.model.enums.ProductFeedbackCategory;
import com.br.devsami.model.repository.ProductRepository;

public class TotemTools {

        private final EmployeeRepository employeeRepository = new EmployeeRepository();
        private final ProductRepository productRepository = new ProductRepository();
        private final FeedbackAnalyticsService analyticsService = new FeedbackAnalyticsService();

        /**
         * PASSO 1: Descobrir o ID numérico.
         * EXEMPLO DO FLUXO:
         * 1. User: "Quero a distribuição do João".
         * 2. IA chama esta tool com "João".
         * 3. Retorna: "Opção 1: João (ID do BD: 5 - GERENTE)".
         * 4. IA para e pergunta ao User se é esse mesmo (escondendo o ID do BD).
         */
        @Tool("Pesquisa funcionários pelo nome. Usar PRIMEIRO para descobrir o ID exato caso não saiba.")
        public String buscarFuncionarioPorNome(String nome) {
                List<Employee> employees = employeeRepository.findByName(nome);
                if (employees.isEmpty())
                        return "Nenhum funcionário encontrado.";

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < employees.size(); i++) {
                        Employee e = employees.get(i);
                        sb.append(String.format("Opção %d: %s (ID do BD: %d - %s)\n",
                                        i + 1, e.getName(), e.getId(), e.getTipo()));
                }
                return sb.toString();
        }

        /**
         * PASSO 2: Calcular e montar o gráfico.
         * EXEMPLO DO FLUXO:
         * 1. User responde: "Isso, o gerente".
         * 2. IA lê o histórico, sabe que o ID é 5, e chama esta tool passando
         * employeeId=5.
         * 3. Tool converte datas, calcula as porcentagens e já devolve a string pronta.
         * 4. IA apenas repassa essa string pro JavaFX interceptar e desenhar o gráfico.
         */
        @Tool("Gera o gráfico de PIZZA com a DISTRIBUIÇÃO GERAL DE SATISFAÇÃO (Satisfeito, Neutro, Insatisfeito) de um funcionário. O employeeId é OBRIGATÓRIO. Datas opcionais.")
        public String gerarRelatorioDeSatisfacao(Long employeeId, String startDate, String endDate) {
                Employee employee = employeeRepository.findById(employeeId).orElse(null);
                if (employee == null || !employee.isAtivo()) {
                        return "Funcionário inativo ou inexistente. Não é possível gerar relatório.";
                }

                // Conversão das strings enviadas pela IA para datas válidas
                LocalDateTime start = (startDate != null && !startDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(startDate).atStartOfDay()
                                : null;
                LocalDateTime end = (endDate != null && !endDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(endDate).atTime(LocalTime.MAX)
                                : null;

                // Processamento direto (sem devolver vetor solto pra IA)
                double[] percentagens = analyticsService.calcularPercentagens(employeeId, start, end);
                if (percentagens[0] == 0.0 && percentagens[1] == 0.0 && percentagens[2] == 0.0) {
                        return "[SEM_DADOS]";
                }

                String nomeFuncionario = employee.getName();
                ChartManager.exibirPizza("Satisfação - " + nomeFuncionario, percentagens);

                return String.format("Gráfico de Pizza gerado na tela para %s: %.2f%% satisfação, %.2f%% neutralidade e %.2f%% insatisfação.",
                        nomeFuncionario, percentagens[0], percentagens[1], percentagens[2]);
        }

        // Tool responsavel por pegar o funcionário com maior indece de X sentimento
        @Tool("Busca o funcionário com maior taxa. Índices: 0=Satisfeito, 1=Neutro, 2=Insatisfeito. Datas opcionais.")
        public String buscarMaiorTaxa(@P("Índice de sentimento: 0=Satisfeito, 1=Neutro, 2=Insatisfeito") int indice,
                                      @P("Data de início no formato YYYY-MM-DD ou null") String startDate,
                                      @P("Data de fim no formato YYYY-MM-DD ou null") String endDate) {
                List<Employee> todos = employeeRepository.findAllActive();

                LocalDateTime start = (startDate != null && !startDate.equals("null"))
                                ? LocalDate.parse(startDate).atStartOfDay()
                                : null;
                LocalDateTime end = (endDate != null && !endDate.equals("null"))
                                ? LocalDate.parse(endDate).atTime(LocalTime.MAX)
                                : null;

                return analyticsService.obterMaiorTaxa(todos, indice, start, end);
        }

        @Tool("Gera o gráfico de LINHAS com a EVOLUÇÃO TEMPORAL ao longo dos dias de um funcionário. O employeeId é OBRIGATÓRIO. Datas opcionais.")
        public String gerarGraficoEvolucaoTemporal(Long employeeId, String startDate, String endDate) {
                Employee employee = employeeRepository.findById(employeeId).orElse(null);
                if (employee == null || !employee.isAtivo()) {
                        return "Funcionário inativo ou inexistente. Não é possível gerar gráfico.";
                }

                LocalDateTime start = (startDate != null && !startDate.equalsIgnoreCase("null")) ? LocalDate.parse(startDate).atStartOfDay() : null;
                LocalDateTime end = (endDate != null && !endDate.equalsIgnoreCase("null")) ? LocalDate.parse(endDate).atTime(LocalTime.MAX) : null;

                Map<String, int[]> dados = analyticsService.evolucaoSatisfacao(employeeId, start, end);
                String nome = employee.getName();

                ChartManager.exibirLinhas("Evolução Temporal - " + nome, dados);

                return String.format("Gráfico de linhas gerado na tela com sucesso para %s.", nome);
        }

        // =========================================================================
        // NOVAS FERRAMENTAS PARA PRODUTOS
        // =========================================================================

        @Tool("Pesquisa produtos pelo nome. Usar PRIMEIRO para descobrir o ID exato do produto caso não saiba.")
        public String buscarProdutoPorNome(String nome) {
                List<Product> produtos = productRepository.findByNameContainingIgnoreCase(nome);
                if (produtos.isEmpty()) {
                        return "Nenhum produto encontrado com o nome '" + nome + "'.";
                }

                com.br.devsami.model.repository.FeedbackRepository feedbackRepo = new com.br.devsami.model.repository.FeedbackRepository();
                List<com.br.devsami.model.dto.ProductCardData> cardList = new java.util.ArrayList<>();

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < produtos.size(); i++) {
                        Product p = produtos.get(i);
                        sb.append(String.format("Opção %d: %s (ID do BD: %s - Preço: R$ %.2f)\n",
                                        i + 1, p.getName(), p.getId().toString(), p.getPrice()));

                        List<com.br.devsami.model.entity.ProductFeedback> feedbacks = feedbackRepo.findByProductId(p.getId());
                        double soma = 0.0;
                        int total = 0;
                        for (com.br.devsami.model.entity.ProductFeedback pf : feedbacks) {
                                if (pf.getRating() != null) {
                                        soma += pf.getRating();
                                        total++;
                                }
                        }
                        double media = total > 0 ? Math.round((soma / total) * 100.0) / 100.0 : 0.0;
                        cardList.add(new com.br.devsami.model.dto.ProductCardData(p.getId(), p.getName(), p.getPrice(), media, total));
                }

                ChartManager.exibirCardsProdutos("Produtos Encontrados (" + produtos.size() + ")", cardList);
                return sb.toString();
        }

        @Tool("Busca produtos com média de estrelas dentro de um intervalo (ex: minRating=4.0, maxRating=5.0 para excelentes, ou minRating=0.0, maxRating=3.0 para notas baixas). Datas opcionais (YYYY-MM-DD ou 'null').")
        public String buscarProdutosPorFaixaDeNota(Double minRating, Double maxRating, String startDate, String endDate) {
                double min = (minRating != null) ? minRating : 0.0;
                double max = (maxRating != null) ? maxRating : 5.0;

                LocalDateTime start = (startDate != null && !startDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(startDate).atStartOfDay()
                                : null;
                LocalDateTime end = (endDate != null && !endDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(endDate).atTime(LocalTime.MAX)
                                : null;

                List<Product> todos = productRepository.findAll();
                com.br.devsami.model.repository.FeedbackRepository feedbackRepo = new com.br.devsami.model.repository.FeedbackRepository();
                List<com.br.devsami.model.dto.ProductCardData> cardList = new java.util.ArrayList<>();

                for (Product p : todos) {
                        List<com.br.devsami.model.entity.ProductFeedback> feedbacks = feedbackRepo.findProductFeedbacksByProductAndPeriod(p.getId(), start, end);
                        if (feedbacks.isEmpty()) continue;

                        double soma = 0.0;
                        int totalVotos = 0;
                        for (com.br.devsami.model.entity.ProductFeedback pf : feedbacks) {
                                if (pf.getRating() != null) {
                                        soma += pf.getRating();
                                        totalVotos++;
                                }
                        }

                        if (totalVotos > 0) {
                                double media = Math.round((soma / totalVotos) * 100.0) / 100.0;
                                if (media >= min && media <= max) {
                                        cardList.add(new com.br.devsami.model.dto.ProductCardData(p.getId(), p.getName(), p.getPrice(), media, totalVotos));
                                }
                        }
                }

                String titulo = String.format("Produtos com Nota %.1f a %.1f ⭐", min, max);
                ChartManager.exibirCardsProdutos(titulo, cardList);

                return analyticsService.obterProdutosPorFaixaDeNota(min, max, start, end);
        }

        @Tool("Gera gráfico de PIZZA com a distribuição geral de todas as categorias de produtos (Sabor, Temperatura, Porção, Embalagem, Preço, Elogios). Datas opcionais.")
        public String gerarGraficoGeralCategoriasProduto(String startDate, String endDate) {
                LocalDateTime start = (startDate != null && !startDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(startDate).atStartOfDay()
                                : null;
                LocalDateTime end = (endDate != null && !endDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(endDate).atTime(LocalTime.MAX)
                                : null;

                Map<ProductFeedbackCategory, Double> distribuicao = analyticsService.calcularDistribuicaoCategoriasProdutoGeral(start, end);
                if (distribuicao.isEmpty()) {
                        return "Nenhum feedback de produto cadastrado no período para gerar gráfico.";
                }

                double[] percentagens = new double[ProductFeedbackCategory.values().length];
                int idx = 0;
                for (ProductFeedbackCategory cat : ProductFeedbackCategory.values()) {
                        percentagens[idx++] = distribuicao.getOrDefault(cat, 0.0);
                }

                ChartManager.exibirPizza("Distribuição Geral de Categorias de Produtos", percentagens);

                StringBuilder sb = new StringBuilder("Gráfico de Pizza gerado na tela com a distribuição de categorias:\n");
                for (ProductFeedbackCategory cat : ProductFeedbackCategory.values()) {
                        sb.append(String.format("- %s: %.2f%%\n", cat.getDescription(), distribuicao.getOrDefault(cat, 0.0)));
                }

                return sb.toString();
        }

        @Tool("Retorna a média de notas em estrelas agrupada por cada categoria de produto (Sabor, Temperatura, Porção, Embalagem, Preço, Elogios). Datas opcionais.")
        public String obterMediaEstrelasPorCategoriaProduto(String startDate, String endDate) {
                LocalDateTime start = (startDate != null && !startDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(startDate).atStartOfDay()
                                : null;
                LocalDateTime end = (endDate != null && !endDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(endDate).atTime(LocalTime.MAX)
                                : null;

                return analyticsService.calcularMediaEstrelasPorCategoriaProduto(start, end);
        }

        @Tool("Gera gráfico de PIZZA com a distribuição das categorias para UM PRODUTO ESPECÍFICO. O productId é OBRIGATÓRIO (UUID do banco). Datas opcionais.")
        public String gerarGraficoCategoriasDoProduto(String productId, String startDate, String endDate) {
                java.util.UUID uuid;
                try {
                        uuid = java.util.UUID.fromString(productId);
                } catch (Exception e) {
                        return "ID do produto inválido. Use a busca por nome primeiro.";
                }

                Product product = productRepository.findById(uuid).orElse(null);
                if (product == null) {
                        return "Produto inativo ou inexistente. Não é possível gerar gráfico.";
                }

                LocalDateTime start = (startDate != null && !startDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(startDate).atStartOfDay()
                                : null;
                LocalDateTime end = (endDate != null && !endDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(endDate).atTime(LocalTime.MAX)
                                : null;

                Map<ProductFeedbackCategory, Double> distribuicao = analyticsService.calcularDistribuicaoCategoriasProdutoIndividual(uuid, start, end);
                if (distribuicao.isEmpty()) {
                        return "Nenhum feedback registrado para o produto '" + product.getName() + "' no período.";
                }

                double[] percentagens = new double[ProductFeedbackCategory.values().length];
                int idx = 0;
                for (ProductFeedbackCategory cat : ProductFeedbackCategory.values()) {
                        percentagens[idx++] = distribuicao.getOrDefault(cat, 0.0);
                }

                String titulo = "Categorias - " + product.getName();
                ChartManager.exibirPizza(titulo, percentagens);

                StringBuilder sb = new StringBuilder("Gráfico de Pizza gerado na tela para o produto '" + product.getName() + "':\n");
                for (ProductFeedbackCategory cat : ProductFeedbackCategory.values()) {
                        sb.append(String.format("- %s: %.2f%%\n", cat.getDescription(), distribuicao.getOrDefault(cat, 0.0)));
                }

                return sb.toString();
        }

        @Tool("Gera gráfico de PIZZA com a distribuição de ESTRELAS (1 a 5 ⭐) de um PRODUTO ESPECÍFICO. O productId é OBRIGATÓRIO (UUID do banco). Datas opcionais.")
        public String gerarGraficoEstrelasDoProduto(String productId, String startDate, String endDate) {
                java.util.UUID uuid;
                try {
                        uuid = java.util.UUID.fromString(productId);
                } catch (Exception e) {
                        return "ID do produto inválido. Use a busca por nome primeiro.";
                }

                Product product = productRepository.findById(uuid).orElse(null);
                if (product == null) {
                        return "Produto inativo ou inexistente. Não é possível gerar gráfico.";
                }

                LocalDateTime start = (startDate != null && !startDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(startDate).atStartOfDay()
                                : null;
                LocalDateTime end = (endDate != null && !endDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(endDate).atTime(LocalTime.MAX)
                                : null;

                double[] percentagens = analyticsService.calcularDistribuicaoEstrelasProduto(uuid, start, end);

                String titulo = "Notas (Estrelas) - " + product.getName();
                ChartManager.exibirPizza(titulo, percentagens);

                String[] labels = {"5 ⭐ (Excelente)", "4 ⭐ (Muito Bom)", "3 ⭐ (Bom)", "2 ⭐ (Ruim)", "1 ⭐ (Péssimo)"};
                StringBuilder sb = new StringBuilder("Gráfico de Pizza com a distribuição de notas (estrelas) gerado na tela para '" + product.getName() + "':\n");
                for (int i = 0; i < percentagens.length; i++) {
                        sb.append(String.format("- %s: %.2f%%\n", labels[i], percentagens[i]));
                }

                return sb.toString();
        }

        @Tool("Gera gráfico de PIZZA com a distribuição GERAL de categorias de atendimento de toda a equipe (Cortesia, Agilidade, Comunicação, Resolução, Elogios, Outros). Datas opcionais.")
        public String gerarGraficoGeralCategoriasAtendimento(String startDate, String endDate) {
                LocalDateTime start = (startDate != null && !startDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(startDate).atStartOfDay()
                                : null;
                LocalDateTime end = (endDate != null && !endDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(endDate).atTime(LocalTime.MAX)
                                : null;

                Map<com.br.devsami.model.enums.EmployeeFeedbackCategory, Double> distribuicao = analyticsService.calcularDistribuicaoCategoriasAtendimentoGeral(start, end);
                if (distribuicao.isEmpty()) {
                        return "Nenhum feedback de atendimento cadastrado no período para gerar gráfico.";
                }

                double[] percentagens = new double[com.br.devsami.model.enums.EmployeeFeedbackCategory.values().length];
                int idx = 0;
                for (com.br.devsami.model.enums.EmployeeFeedbackCategory cat : com.br.devsami.model.enums.EmployeeFeedbackCategory.values()) {
                        percentagens[idx++] = distribuicao.getOrDefault(cat, 0.0);
                }

                ChartManager.exibirPizza("Distribuição Geral de Categorias de Atendimento", percentagens);

                StringBuilder sb = new StringBuilder("Gráfico de Pizza gerado na tela com a distribuição das categorias de atendimento:\n");
                for (com.br.devsami.model.enums.EmployeeFeedbackCategory cat : com.br.devsami.model.enums.EmployeeFeedbackCategory.values()) {
                        sb.append(String.format("- %s: %.2f%%\n", cat.getDescription(), distribuicao.getOrDefault(cat, 0.0)));
                }

                return sb.toString();
        }

        @Tool("Gera gráfico de PIZZA com a distribuição das categorias de atendimento para UM FUNCIONÁRIO ESPECÍFICO (Cortesia, Agilidade, Comunicação, Resolução, Elogios, Outros). O employeeId é OBRIGATÓRIO. Datas opcionais.")
        public String gerarGraficoCategoriasDoFuncionario(Long employeeId, String startDate, String endDate) {
                Employee employee = employeeRepository.findById(employeeId).orElse(null);
                if (employee == null) {
                        return "Funcionário inativo ou não encontrado no sistema.";
                }

                LocalDateTime start = (startDate != null && !startDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(startDate).atStartOfDay()
                                : null;
                LocalDateTime end = (endDate != null && !endDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(endDate).atTime(LocalTime.MAX)
                                : null;

                Map<com.br.devsami.model.enums.EmployeeFeedbackCategory, Double> distribuicao = analyticsService.calcularDistribuicaoCategoriasAtendimentoIndividual(employeeId, start, end);
                if (distribuicao.isEmpty()) {
                        return "Nenhum feedback registrado para o funcionário '" + employee.getName() + "' no período.";
                }

                double[] percentagens = new double[com.br.devsami.model.enums.EmployeeFeedbackCategory.values().length];
                int idx = 0;
                for (com.br.devsami.model.enums.EmployeeFeedbackCategory cat : com.br.devsami.model.enums.EmployeeFeedbackCategory.values()) {
                        percentagens[idx++] = distribuicao.getOrDefault(cat, 0.0);
                }

                String titulo = "Categorias - " + employee.getName();
                ChartManager.exibirPizza(titulo, percentagens);

                StringBuilder sb = new StringBuilder("Gráfico de Pizza gerado na tela para o funcionário '" + employee.getName() + "':\n");
                for (com.br.devsami.model.enums.EmployeeFeedbackCategory cat : com.br.devsami.model.enums.EmployeeFeedbackCategory.values()) {
                        sb.append(String.format("- %s: %.2f%%\n", cat.getDescription(), distribuicao.getOrDefault(cat, 0.0)));
                }

                return sb.toString();
        }

        @Tool("Busca funcionários ativos por FAIXA DE SATISFAÇÃO (percentual de 0 a 100). Exibe CARDS VISUAIS no painel lateral. Datas opcionais.")
        public String buscarFuncionariosPorFaixaDeSatisfacao(Double minPercent, Double maxPercent, String startDate, String endDate) {
                double min = minPercent != null ? minPercent : 0.0;
                double max = maxPercent != null ? maxPercent : 100.0;

                LocalDateTime start = (startDate != null && !startDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(startDate).atStartOfDay()
                                : null;
                LocalDateTime end = (endDate != null && !endDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(endDate).atTime(LocalTime.MAX)
                                : null;

                List<Employee> todos = employeeRepository.findAllActive();
                com.br.devsami.model.repository.FeedbackRepository feedbackRepo = new com.br.devsami.model.repository.FeedbackRepository();
                List<com.br.devsami.model.dto.EmployeeCardData> cardList = new java.util.ArrayList<>();

                for (Employee e : todos) {
                        double[] taxas = analyticsService.calcularPercentagens(e.getId(), start, end);
                        double sat = taxas[0];
                        if (sat >= min && sat <= max) {
                                List<com.br.devsami.model.entity.EmployeeFeedback> fbs = (start != null && end != null)
                                                ? feedbackRepo.findByEmployeeAndPeriod(e.getId(), start, end)
                                                : feedbackRepo.findByEmployeeId(e.getId());
                                cardList.add(new com.br.devsami.model.dto.EmployeeCardData(e.getId(), e.getName(), e.getTipo(), sat, taxas[1], taxas[2], fbs.size()));
                        }
                }

                String titulo = String.format("Funcionários com Satisfação %.0f%% a %.0f%%", min, max);
                ChartManager.exibirCardsFuncionarios(titulo, cardList);

                return analyticsService.obterFuncionariosPorFaixaDeSatisfacao(min, max, start, end);
        }
}
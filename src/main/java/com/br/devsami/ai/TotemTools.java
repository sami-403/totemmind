package com.br.devsami.ai;

import dev.langchain4j.agent.tool.Tool;
import com.br.devsami.model.repository.EmployeeRepository;
import com.br.devsami.model.entity.Employee;
import com.br.devsami.model.service.FeedbackAnalyticsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class TotemTools {

        private final EmployeeRepository employeeRepository = new EmployeeRepository();
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
        @Tool("Calcula os dados de satisfação e já gera o comando do gráfico. O employeeId é OBRIGATÓRIO. startDate e endDate são OPCIONAIS (YYYY-MM-DD ou 'null').")
        public String gerarRelatorioDeSatisfacao(Long employeeId, String startDate, String endDate,
                        String tipoGrafico) {

                // Conversão das strings enviadas pela IA para datas válidas
                LocalDateTime start = (startDate != null && !startDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(startDate).atStartOfDay()
                                : null;
                LocalDateTime end = (endDate != null && !endDate.equalsIgnoreCase("null"))
                                ? LocalDate.parse(endDate).atTime(LocalTime.MAX)
                                : null;

                // Processamento direto (sem devolver vetor solto pra IA)
                double[] percentagens = analyticsService.calcularPercentagens(employeeId, start, end);

                // Busca o nome real para o título do gráfico
                String nomeFuncionario = employeeRepository.findById(employeeId)
                                .map(Employee::getName)
                                .orElse("Desconhecido");

                // Adiciona saida para auxiliar no desenvolvimento
                System.out.println("Satisfeitos: " + percentagens[0]);
                System.out.println("Neutro: " + percentagens[1]);
                System.out.println("Insatisfeitos: " + percentagens[2]);

                // Retorna o comando final formatado (Futuramente será alterada para a tool real
                // com o comportamento na webview)
                return String.format("[COMANDO_GRAFICO] TIPO: %s | FUNCIONARIO: %s | DADOS: S:%.2f N:%.2f I:%.2f",
                                tipoGrafico, nomeFuncionario, percentagens[0], percentagens[1], percentagens[2]);
        }

        // Tool responsavel por pegar o funcionário com maior indece de X sentimento
        @Tool("Busca o funcionário com maior taxa. Índices: 0=Satisfeito, 1=Neutro, 2=Insatisfeito. Datas opcionais.")
        public String buscarMaiorTaxa(int indice, String startDate, String endDate) {
                List<Employee> todos = employeeRepository.findAll();

                LocalDateTime start = (startDate != null && !startDate.equals("null"))
                                ? LocalDate.parse(startDate).atStartOfDay()
                                : null;
                LocalDateTime end = (endDate != null && !endDate.equals("null"))
                                ? LocalDate.parse(endDate).atTime(LocalTime.MAX)
                                : null;

                return analyticsService.obterMaiorTaxa(todos, indice, start, end);
        }
}
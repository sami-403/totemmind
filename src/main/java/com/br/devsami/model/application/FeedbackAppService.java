package com.br.devsami.model.application;

import java.util.UUID;

import com.br.devsami.model.entity.Employee;
import com.br.devsami.model.entity.Feedback;
import com.br.devsami.model.entity.User;
import com.br.devsami.model.service.EmployeeService;
import com.br.devsami.model.service.FeedbackService;
import com.br.devsami.model.service.UserService;
import com.br.devsami.utils.enums.FeedbackCategory;
import com.br.devsami.utils.enums.Feelling;

/**
 * Camada de aplicação responsável por orquestrar o fluxo completo de criação de
 * feedback.
 *
 * Aqui não existe regra de negócio pesada:
 * apenas coordenação entre services.
 */
public class FeedbackAppService {

    private final FeedbackService feedbackService;
    private final UserService userService;
    private final EmployeeService employeeService;

    /**
     * Injeta os serviços necessários para o fluxo.
     */
    public FeedbackAppService(
            FeedbackService feedbackService,
            UserService userService,
            EmployeeService employeeService) {
        this.feedbackService = feedbackService;
        this.userService = userService;
        this.employeeService = employeeService;
    }

    /**
     * Fluxo principal do sistema de feedback:
     * 1. Busca o usuário pelo CPF
     * 2. Busca o funcionário pelo ID
     * 3. Cria e salva o feedback
     *
     * Essa camada existe para evitar que a UI (JavaFX)
     * tenha que lidar com múltiplos services diretamente.
     */
    public Feedback registerFeedback(String cpf,
            UUID employeeId,
            Feelling feeling,
            FeedbackCategory category,
            String text) {

        // Recupera o usuário responsável pelo feedback
        User user = userService.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("User não encontrado"));

        // Recupera o funcionário que recebeu o feedback
        Employee employee = employeeService.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee não encontrado"));

        // Cria e persiste o feedback via service especializado
        return feedbackService.createFeedback(
                user,
                employee,
                feeling,
                category,
                text);
    }
}
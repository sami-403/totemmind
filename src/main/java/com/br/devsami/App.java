package com.br.devsami;

import java.time.LocalDate;

import com.br.devsami.model.entity.Employee;
import com.br.devsami.model.entity.Feedback;
import com.br.devsami.model.entity.User;
import com.br.devsami.model.service.EmployeeService;
import com.br.devsami.model.service.FeedbackService;
import com.br.devsami.model.service.UserService;
import com.br.devsami.utils.enums.EmployeeType;
import com.br.devsami.utils.enums.FeedbackCategory;
import com.br.devsami.utils.enums.Feelling;

public class App {

        public static void main(String[] args) {

                UserService userService = new UserService();
                EmployeeService employeeService = new EmployeeService();
                FeedbackService feedbackService = new FeedbackService();

                Employee employee = employeeService.createEmployee(
                                "João",
                                "12345678900",
                                EmployeeType.GERENTE);

                User user = userService.findOrCreateUser(
                                "Carlos",
                                "11122233344",
                                LocalDate.now());

                Feedback feedback = feedbackService.createFeedback(
                                user,
                                employee,
                                Feelling.SATISFIED,
                                FeedbackCategory.SERVICE_QUALITY,
                                "Atendimento muito bom");

                System.out.println("Feedback salvo com sucesso: " + feedback.getId());
        }
}
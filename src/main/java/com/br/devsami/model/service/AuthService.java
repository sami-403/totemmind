package com.br.devsami.model.service;

// classe responsavel pela atenticação do gerente no totem, por meio do cpf

import com.br.devsami.model.entity.Employee;
import com.br.devsami.utils.enums.EmployeeType;
import com.br.devsami.model.repository.EmployeeRepository;

public class AuthService {
    private final EmployeeRepository employeeRepository;

    public AuthService() {
        this.employeeRepository = new EmployeeRepository();
    }

    public Employee login(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF não pode ser vazio");
        }
        // Remove formatação (pontos, traços)
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        return employeeRepository.findByCpf(cpfLimpo)
                .orElseThrow(() -> new RuntimeException("CPF não encontrado"));
    }

    public boolean isGerente(Employee employee) {
        return employee.getTipo() == EmployeeType.GERENTE;
    }
}
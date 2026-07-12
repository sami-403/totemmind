package com.br.devsami.model.service;

import java.util.List;
import java.util.Optional;

import com.br.devsami.model.entity.Employee;
import com.br.devsami.model.repository.EmployeeRepository;
import com.br.devsami.infrastructure.persistence.HibernateUtil;
import com.br.devsami.model.enums.EmployeeType;
import jakarta.persistence.EntityManager;

public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService() {
        this.employeeRepository = new EmployeeRepository();
    }

    public Employee createEmployee(String name, String cpf, EmployeeType type, String password) {
        // ... validações de nome e cpf ...

        var employee = new Employee();
        employee.setName(name);
        employee.setCpf(cpf);
        employee.setTipo(type);

        // Salva a senha. Se for nulo, o banco aceitará como nulo.
        employee.setPassword(password);

        employeeRepository.save(employee);
        return employee;
    }

    public Optional<Employee> findById(long id) {
        return employeeRepository.findById(id);
    }

    public List<Employee> findByName(String name) {
        return employeeRepository.findByName(name);
    }

    public Employee updateEmployee(long id, String newName, EmployeeType newType, String senha) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com o ID fornecido."));

        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Nome obrigatório.");
        }

        employee.setName(newName);
        employee.setTipo(newType);

        employeeRepository.update(employee);

        return employee;
    }

    /*
     * Exclusão Lógica (Soft Delete).
     * Em vez de apagar do banco (o que causaria erro de Chave Estrangeira),
     * nós desativamos o funcionário.
     */
    public void deleteEmployee(long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com o ID fornecido."));

        if (!employee.isAtivo()) {
            throw new IllegalStateException("Este funcionário já está removido/inativo.");
        }

        // Troca o status para false (inativo) e atualiza no banco
        employee.setAtivo(false);
        employeeRepository.update(employee);
    }

    // autenticação geral
    public void authenticateManager(String cpf, String senha) {
        // Busca o funcionário pelo CPF
        Employee employee = employeeRepository.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado."));

        // Verifica se o funcionário foi inativado (Soft Delete)
        if (!employee.isAtivo()) {
            throw new IllegalStateException("Acesso negado: Funcionário inativo.");
        }

        // Verifica a senha
        if (!senha.equals(employee.getPassword())) {
            throw new IllegalArgumentException("Senha incorreta.");
        }

        // Verifica se o cargo é GERENTE
        if (employee.getTipo() != EmployeeType.GERENTE) {
            throw new SecurityException("Acesso negado: Apenas gerentes podem acessar esta área.");
        }
    }
}
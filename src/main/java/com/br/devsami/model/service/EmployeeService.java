package com.br.devsami.model.service;

import java.util.List;
import java.util.Optional;

import com.br.devsami.model.entity.Employee;
import com.br.devsami.model.repository.EmployeeRepository;
import com.br.devsami.model.enums.EmployeeType;
import com.br.devsami.util.CpfValidator;

public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService() {
        this.employeeRepository = new EmployeeRepository();
    }

    public Employee createEmployee(String name, String cpf, EmployeeType type, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome obrigatório.");
        }

        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF obrigatório.");
        }

        String validationError = CpfValidator.validate(cpf);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        if (employeeRepository.existsByCpf(cpf)) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }

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

    public List<Employee> listAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee updateEmployee(long id, String newName, String newCpf, EmployeeType newType, String senha) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com o ID fornecido."));

        if (!employee.isAtivo()) {
            employee.setAtivo(true);
        }

        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Nome obrigatório.");
        }

        if (newCpf == null || newCpf.isBlank()) {
            throw new IllegalArgumentException("CPF obrigatório.");
        }

        String validationError = CpfValidator.validate(newCpf);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        // Se o CPF mudou, verifica se já está em uso por outro funcionário
        if (!newCpf.equals(employee.getCpf())) {
            if (employeeRepository.existsByCpf(newCpf)) {
                throw new IllegalArgumentException("CPF já cadastrado por outro funcionário.");
            }
            employee.setCpf(newCpf);
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
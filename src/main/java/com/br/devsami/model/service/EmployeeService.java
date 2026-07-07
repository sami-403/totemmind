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

    public Employee createEmployee(String name, String cpf, EmployeeType type) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome obrigatório");
        }

        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF obrigatório");
        }

        if (employeeRepository.existsByCpf(cpf)) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }

        var employee = new Employee();
        employee.setName(name);
        employee.setCpf(cpf);
        employee.setTipo(type);

        employeeRepository.save(employee);

        return employee;
    }

    public Optional<Employee> findById(long id) {
        return employeeRepository.findById(id);
    }

    public List<Employee> findByName(String name) {
        EntityManager em = HibernateUtil.getEntityManager();

        try {
            return em.createQuery(
                            "SELECT e FROM Employee e WHERE LOWER(e.name) LIKE LOWER(:name)",
                            Employee.class)
                    .setParameter("name", "%" + name.trim() + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Employee updateEmployee(long id, String newName, EmployeeType newType) {
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
}
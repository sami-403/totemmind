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

    /*
     * Injeção de dependência do repository.
     */
    public EmployeeService() {
        this.employeeRepository = new EmployeeRepository();
    }

    /*
     * Criação de um novo funcionário.
     *
     * Regras de negócio:
     * - Nome não pode ser vazio
     * - CPF não pode ser vazio
     * - CPF deve ser único no sistema
     */
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

    /*
     * Busca funcionário pelo ID.
     */
    public Optional<Employee> findById(long id) {
        return employeeRepository.findById(id);
    }

    // buscar por nome (Devolve uma lista dos funcionários com aquele nome)
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

    /*
     * Edição de um funcionário existente.
     * * Regras de negócio:
     * - O funcionário deve existir no banco de dados.
     * - O novo nome não pode ser nulo ou vazio.
     */
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
     * Remoção de um funcionário do sistema.
     * * Regras de negócio:
     * - Certifica-se de que o funcionário existe antes de solicitar a remoção.
     */
    public void deleteEmployee(long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com o ID fornecido."));

        employeeRepository.delete(employee);
    }
}
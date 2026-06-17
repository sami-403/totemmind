package com.br.devsami.model.service;

import java.util.List;
import java.util.Optional;

import com.br.devsami.model.entity.Employee;
import com.br.devsami.model.repository.EmployeeRepository;
import com.br.devsami.utils.HibernateUtil;
import com.br.devsami.utils.enums.EmployeeType;
import jakarta.persistence.EntityManager;

public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    /*
     * Injeção de dependência do repository.
     * Alternativa simples (sem DI):
     *
     * public EmployeeService() {
     * this.employeeRepository = new EmployeeRepository();
     * }
     *
     * Aqui estamos preferindo injeção para manter o serviço desacoplado
     * e facilitar testes e manutenção.
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
     *
     * Responsabilidade: validar dados + orquestrar persistência.
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
     *
     * Apenas delega para o repository.
     * O Optional é retornado para o chamador decidir o que fazer
     * caso não exista.
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
}
package com.br.devsami.model.service;

import com.br.devsami.model.entity.Employee;
import com.br.devsami.model.repository.EmployeeRepository;
import com.br.devsami.utils.enums.EmployeeType;

public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    // instancia, coloquei com injeção de dependencia, mas poderia ser:

    /*

    public EmployeeService() {
        this.employeeRepository = new EmployeeRepository();
    }
    */
    
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
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

        Employee employee = new Employee();
        employee.setName(name);
        employee.setCpf(cpf);
        employee.setTipo(type);

        employeeRepository.save(employee);

        return employee;
    }
}
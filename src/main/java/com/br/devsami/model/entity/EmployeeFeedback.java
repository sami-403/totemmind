package com.br.devsami.model.entity;

import com.br.devsami.model.enums.EmployeeFeedbackCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("EMPLOYEE")
@Getter
@Setter
@NoArgsConstructor
// Entidade de feedback de funcionário
public class EmployeeFeedback extends Feedback {

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = true)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_category")
    private EmployeeFeedbackCategory employeeCategory;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public EmployeeFeedbackCategory getEmployeeCategory() {
        return employeeCategory;
    }

    public void setEmployeeCategory(EmployeeFeedbackCategory employeeCategory) {
        this.employeeCategory = employeeCategory;
    }
}

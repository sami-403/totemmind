package com.br.devsami.model.dto;

import com.br.devsami.model.enums.EmployeeType;

public record EmployeeCardData(
        Long id,
        String name,
        EmployeeType tipo,
        double pctSatisfied,
        double pctNeutral,
        double pctDissatisfied,
        int totalFeedbacks
) {}

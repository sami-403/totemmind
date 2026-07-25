package com.br.devsami.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductCardData(
        UUID id,
        String name,
        BigDecimal price,
        double ratingAverage,
        int totalFeedbacks
) {}

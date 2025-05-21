package com.vserdiuk.casestudy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductDTO {
    @NotNull
    private Long id;

    private String name;

    @Min(0)
    private BigDecimal price;

    @Min(0)
    private Integer stockQuantity;
}

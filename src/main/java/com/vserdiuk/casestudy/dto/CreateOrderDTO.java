package com.vserdiuk.casestudy.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderDTO {

    @NotEmpty
    private List<OrderProductDTO> items;
}

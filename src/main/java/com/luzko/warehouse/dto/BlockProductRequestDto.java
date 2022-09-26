package com.luzko.warehouse.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class BlockProductRequestDto {
    @NotEmpty(message = "Product code should not be empty")
    private String productCode;
    @Pattern(regexp = "\\d-\\d{2}-\\d{3}$")
    private String address;
    @Valid
    private BlockProductDto blockInfo;
}

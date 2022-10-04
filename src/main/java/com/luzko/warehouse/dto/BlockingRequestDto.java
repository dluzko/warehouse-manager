package com.luzko.warehouse.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class BlockingRequestDto {
    @NotEmpty(message = "Product code should not be empty")
    @Pattern(regexp = "PROD-\\d{5}$")
    private String productCode;
    @NotEmpty(message = "Product code should not be empty")
    @Pattern(regexp = "\\d-\\d{2}-\\d{3}$")
    private String address;
    @Valid
    private BlockingDto blockingInfo;
}

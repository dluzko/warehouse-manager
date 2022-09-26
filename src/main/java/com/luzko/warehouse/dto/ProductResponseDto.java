package com.luzko.warehouse.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Data
@Accessors(chain = true)
public class ProductResponseDto {
    @NotEmpty(message = "Address should not be empty")
    private String address;
    @Valid
    private ItemDto product;
}

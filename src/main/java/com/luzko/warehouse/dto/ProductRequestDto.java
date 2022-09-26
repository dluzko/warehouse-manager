package com.luzko.warehouse.dto;

import com.luzko.warehouse.model.MeasureUnit;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class ProductRequestDto {
    @NotEmpty(message = "Product code should not be empty")
    @Pattern(regexp = "PROD-\\d{5}$")
    private String productCode;
    @NotEmpty(message = "Name should not be empty")
    @Pattern(regexp = "[A-Za-z\\d]{2,30}$")
//    @Size(min = 2, max = 30, message = "Name should be between 2 and 30 characters")
    private String name;
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Measure unit should not be empty")
    private MeasureUnit measureUnit;
    @NotNull(message = "quantity should not be empty")
    private BigDecimal quantity;
    @NotEmpty(message = "Address should not be empty")
    private String address;
}

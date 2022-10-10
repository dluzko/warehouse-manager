package com.luzko.warehouse.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProductResponseDto {
    private String address;
    private ItemDto product;
}

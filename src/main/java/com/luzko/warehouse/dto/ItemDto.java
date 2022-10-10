package com.luzko.warehouse.dto;

import com.luzko.warehouse.model.MeasureUnit;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)
public class ItemDto {
    private String productCode;
    private String name;
    @Enumerated(EnumType.STRING)
    private MeasureUnit measureUnit;
    private BigDecimal quantity;
    List<BlockingDto> blockingInfo;
}

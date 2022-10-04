package com.luzko.warehouse.dto;

import com.luzko.warehouse.model.BlockingReason;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class BlockingDto {
    @NotEmpty(message = "Blocking token should not be empty")
    private String blockingToken;
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Blocking reason should not be empty")
    private BlockingReason blockingReason;
    @NotNull(message = "Blocked quantity should not be empty")
    private BigDecimal blockedQuantity;
}

package com.luzko.warehouse.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ErrorResponseDto {
    private List<ErrorDto> errors = null;

    public ErrorResponseDto errors(List<ErrorDto> errors) {
        this.errors = errors;
        return this;
    }

    public ErrorResponseDto addErrorsItem(ErrorDto errorsItem) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(errorsItem);
        return this;
    }
}

package com.luzko.warehouse.service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST),
    UNHANDLED(HttpStatus.INTERNAL_SERVER_ERROR),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND),
    PRODUCT_CODE_NOT_FOUND_AT_ADDRESS(HttpStatus.NOT_FOUND),
    NOT_ENOUGH_OF_PRODUCT(HttpStatus.BAD_REQUEST),
    BLOCKING_NOT_FOUND(HttpStatus.NOT_FOUND),
    BLOCKED_QUANTITY_IS_NOT_ENOUGH_FOR_UNBLOCKING(HttpStatus.BAD_REQUEST);

    private final HttpStatus httpStatus;
}

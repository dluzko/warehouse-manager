package com.luzko.warehouse.service.exception;

import java.util.Arrays;

public class WarehouseManagerException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String[] args;

    public WarehouseManagerException(final ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.args = null;
    }

    public WarehouseManagerException(final ErrorCode errorCode,
                                              final Object... args) {
        this.errorCode = errorCode;
        this.args = Arrays.stream(args).map(String::valueOf).toArray(String[]::new);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String[] getArgs() {
        return args == null ? null : Arrays.copyOf(args, args.length);
    }
}
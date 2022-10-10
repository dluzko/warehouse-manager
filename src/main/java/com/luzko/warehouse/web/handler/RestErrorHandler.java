/*
 * Copyright 2021 LLC Leroy Merlin Vostok.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.luzko.warehouse.web.handler;

import com.luzko.warehouse.dto.ErrorDto;
import com.luzko.warehouse.dto.ErrorResponseDto;
import com.luzko.warehouse.service.exception.ErrorCode;
import com.luzko.warehouse.service.exception.WarehouseManagerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class RestErrorHandler extends ResponseEntityExceptionHandler {

  private static final String LOG_PREFIX_WARN = "Warn: ";
  private static final String LOG_PREFIX_ERROR = "Error: ";

  private final Pattern messagePlaceholderPattern = Pattern.compile("\\{\\d+?}\\s?");
  private final MessageSource messageSource;

  @ExceptionHandler(WarehouseManagerException.class)
  public ResponseEntity<ErrorResponseDto> handleWarehouseManagerException(final WarehouseManagerException ex) {
    log.warn(LOG_PREFIX_WARN, ex);
    final ErrorCode errorCode = ex.getErrorCode();
    final Object[] args = ex.getArgs();

    return ResponseEntity.status(errorCode.getHttpStatus())
            .body(args == null ? buildErrorResponse(errorCode) : buildErrorResponse(errorCode, args));
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponseDto handleMissingRequestHeader(final MissingRequestHeaderException ex) {
    log.warn(LOG_PREFIX_ERROR, ex);
    return buildErrorResponse(ErrorCode.INVALID_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Object> handleConstraintViolationException(final ConstraintViolationException ex) {
    log.warn(LOG_PREFIX_WARN, ex);
    return handleConstraintViolations(ex.getConstraintViolations());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponseDto handleDataIntegrityViolationException(final DataIntegrityViolationException ex) {
    log.warn(LOG_PREFIX_WARN, ex);
    final Throwable cause = ex.getCause();
    if (cause instanceof org.hibernate.exception.ConstraintViolationException casted) {
      return buildErrorResponse(getErrorCodeByConstraint(casted.getConstraintName()));
    } else {
      return buildErrorResponse(ErrorCode.UNHANDLED, ex.getMessage());
    }
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(final HttpMessageNotReadableException ex,
                                                                final HttpHeaders headers,
                                                                final HttpStatus status,
                                                                final WebRequest request) {
    log.warn(LOG_PREFIX_WARN, ex);
    return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getHttpStatus())
        .body(buildErrorResponse(ErrorCode.INVALID_REQUEST));
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex,
                                                                final HttpHeaders headers,
                                                                final HttpStatus status,
                                                                final WebRequest request) {
    log.warn(LOG_PREFIX_WARN, ex);
    try {
      return handleConstraintViolations(ex.getFieldErrors().stream()
          .map(fieldError -> (ConstraintViolation<?>) fieldError.unwrap(ConstraintViolation.class))
          .collect(Collectors.toSet()));
    } catch (Exception e) {
      return super.handleMethodArgumentNotValid(ex, headers, status, request);
    }
  }

  private ResponseEntity<Object> handleConstraintViolations(final Set<ConstraintViolation<?>> violations) {
    for (final ConstraintViolation<?> violation : violations) {
      if (violation.getConstraintDescriptor() != null && violation.getConstraintDescriptor().getAnnotation() != null) {
        final Annotation annotation = violation.getConstraintDescriptor().getAnnotation();
        final Class<? extends Annotation> annotationType = annotation.annotationType();
      }
    }
    return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getHttpStatus())
        .body(buildErrorResponse(ErrorCode.INVALID_REQUEST));
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponseDto handleGeneralException(final Exception ex) {
    log.error(LOG_PREFIX_ERROR, ex);
    return buildErrorResponse(ErrorCode.UNHANDLED, ex.getMessage());
  }

  private String buildMessage(final String messageKey, final Object... args) {
    final String originalMessage = messageSource.getMessage(messageKey, args, Locale.getDefault());
    return messagePlaceholderPattern.matcher(originalMessage).replaceAll("");
  }

  private ErrorResponseDto buildErrorResponse(final ErrorCode errorCode, final Object... args) {
    return new ErrorResponseDto().addErrorsItem(
        new ErrorDto().setCode(errorCode.name()).setMessage(buildMessage(errorCode.name(), args)));
  }

  private ErrorCode getErrorCodeByConstraint(final String constraint) {
    return switch (constraint) {
      default -> ErrorCode.UNHANDLED;
    };
  }
}

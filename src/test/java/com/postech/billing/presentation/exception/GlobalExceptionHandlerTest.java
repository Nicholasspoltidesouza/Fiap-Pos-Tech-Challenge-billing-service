package com.postech.billing.presentation.exception;

import com.postech.billing.domain.exception.BusinessException;
import com.postech.billing.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldMapExceptions() {
        ProblemDetail notFound = handler.handleNotFound(new ResourceNotFoundException("missing"));
        ProblemDetail business = handler.handleBusiness(new BusinessException("invalid"));
        ProblemDetail conflict = handler.handleIllegalState(new IllegalStateException("conflict"));
        ProblemDetail generic = handler.handleGeneric(new RuntimeException("boom"));

        assertEquals(HttpStatus.NOT_FOUND.value(), notFound.getStatus());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), business.getStatus());
        assertEquals(HttpStatus.CONFLICT.value(), conflict.getStatus());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), generic.getStatus());
    }
}

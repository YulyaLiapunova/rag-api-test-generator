package com.aiqa.ragapitestgenerator.exception;

import com.aiqa.ragapitestgenerator.dto.CustomErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {
    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest mockRequest;

    @Test
    void testHandleUnauthorizedException() {
        // Arrange
        String errorMessage = "Unauthorized access";
        String requestUri = "/test-uri";

        UnauthorizedException exception = new UnauthorizedException(errorMessage);

        when(mockRequest.getDescription(false)).thenReturn("uri=" + requestUri);

        // Act
        ResponseEntity<CustomErrorResponse> responseEntity =
                globalExceptionHandler.handleUnauthorizedException(exception, mockRequest);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

        CustomErrorResponse errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.getStatus());
        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), errorResponse.getError());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals(requestUri, errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }
}

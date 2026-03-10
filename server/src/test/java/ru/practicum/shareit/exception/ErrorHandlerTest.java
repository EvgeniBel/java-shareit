package ru.practicum.shareit.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlerTest {

    private ErrorHandler errorHandler;

    @BeforeEach
    void setUp() {
        errorHandler = new ErrorHandler();
    }

    @Test
    void testHandleNotFoundException() {
        NotFoundException exception = new NotFoundException("Ошибка");

        Map<String, String> response = errorHandler.handleNotFoundException(exception);

        assertNotNull(response);
        assertEquals("Ошибка", response.get("error"));
    }

    @Test
    void testHandleConflictException() {
        ConflictException exception = new ConflictException("Email уже существует");

        Map<String, String> response = errorHandler.handleConflictException(exception);

        assertNotNull(response);
        assertEquals("Email уже существует", response.get("error"));
    }

    @Test
    void testHandleValidationException() {
        ValidationException exception = new ValidationException("Invalid data");

        Map<String, String> response = errorHandler.handleValidationException(exception);

        assertNotNull(response);
        assertTrue(response.get("error").contains("Invalid data"));
    }

    @Test
    void testHandleUnauthorizedAccessException() {
        UnauthorizedAccessException exception = new UnauthorizedAccessException("Доступ запрещен");

        Map<String, String> response = errorHandler.handleUnauthorizedAccessException(exception);

        assertNotNull(response);
        assertEquals("Доступ запрещен", response.get("error"));
    }

    @Test
    void testHandleOtherExceptions() {
        Throwable exception = new RuntimeException("Непредвиденная ошибка");

        Map<String, String> response = errorHandler.handleOtherExceptions(exception);

        assertNotNull(response);
        assertEquals("Внутренняя ошибка сервера", response.get("error"));
    }

    @Test
    void testExceptionHandlers() throws NoSuchMethodException {
        Method notFoundMethod = ErrorHandler.class.getMethod("handleNotFoundException", NotFoundException.class);
        ExceptionHandler notFoundAnnotation = notFoundMethod.getAnnotation(ExceptionHandler.class);
        assertNotNull(notFoundAnnotation);

        Class<?>[] exceptionTypes = notFoundAnnotation.value();
        System.out.println("Exception types array length: " + exceptionTypes.length);

        // Проверяем, что метод принимает NotFoundException
        assertEquals(1, notFoundMethod.getParameterCount());
        assertEquals(NotFoundException.class, notFoundMethod.getParameterTypes()[0]);

        // Проверяем, что метод принимает ConflictException
        Method conflictMethod = ErrorHandler.class.getMethod("handleConflictException", ConflictException.class);
        ExceptionHandler conflictAnnotation = conflictMethod.getAnnotation(ExceptionHandler.class);
        assertNotNull(conflictAnnotation);

        assertEquals(1, conflictMethod.getParameterCount());
        assertEquals(ConflictException.class, conflictMethod.getParameterTypes()[0]);
    }
}
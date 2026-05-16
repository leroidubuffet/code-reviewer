package com.curso.reviewer.service;

/**
 * Error de cliente (4xx). La petición tiene algún problema que un reintento
 * no va a resolver. Resilience4j tiene esta excepción en ignore-exceptions,
 * por lo que no reintentará las llamadas que la lancen.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

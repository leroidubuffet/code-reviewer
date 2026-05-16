package com.curso.reviewer.service;

/**
 * Error transitorio del proveedor LLM (5xx, 429, 529, red).
 * Resilience4j reintentará las llamadas que lancen esta excepción.
 */
public class TransientLlmException extends RuntimeException {
    public TransientLlmException(String message) {
        super(message);
    }
    public TransientLlmException(String message, Throwable cause) {
        super(message, cause);
    }
}

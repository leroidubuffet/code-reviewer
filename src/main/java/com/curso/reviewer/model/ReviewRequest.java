package com.curso.reviewer.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo del POST /review y POST /review/stream.
 *
 * <p>Ejemplo de petición:
 * <pre>
 * {
 *   "language": "java",
 *   "code": "public int suma(int a, int b) { return a - b; }"
 * }
 * </pre>
 */
public record ReviewRequest(
    @NotBlank String language,
    @NotBlank String code
) {}

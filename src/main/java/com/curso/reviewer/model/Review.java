package com.curso.reviewer.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Respuesta estructurada del análisis de código.
 *
 * <p>Spring AI deriva el JSON Schema de este record y lo envía al modelo
 * como restricción de formato. Las anotaciones de Bean Validation se usan
 * para una segunda validación en código después de deserializar, porque el
 * modelo puede cumplir el schema y aun así devolver valores fuera de rango.
 *
 * <p>Igual en la opción A (completa) y en la opción B (TODOs).
 */
public record Review(

    /** Puntuación global del código de 0 (muy mal) a 10 (excelente). */
    @Min(0) @Max(10)
    int score,

    /**
     * Lista de problemas encontrados. Cada elemento es una frase corta
     * que describe un problema concreto. Vacía si no hay problemas.
     */
    @NotEmpty
    List<@NotBlank String> issues,

    /** Resumen ejecutivo del análisis, entre 20 y 500 caracteres. */
    @Size(min = 20, max = 500)
    String summary

    /** Recomendación */
    @Pattern(regexp = "approve|review|reject") String recommendation


) {}

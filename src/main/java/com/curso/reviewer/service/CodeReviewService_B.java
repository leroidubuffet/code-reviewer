package com.curso.reviewer.service;

import com.curso.reviewer.model.Review;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.Set;

/**
 * OPCIÓN B — versión con TODOs para el alumno.
 *
 * Para usar esta versión:
 *   1. Renombrar este fichero a CodeReviewService.java
 *   2. Eliminar CodeReviewService_A.java
 *
 * Criterio de éxito: cuando los cinco eventos son visibles en los logs,
 * el ejercicio está completo.
 *
 *   [1] POST /review recibido con los parámetros de entrada
 *   [2] LLM call OK con el modelo y los parámetros configurados
 *   [3] input_tokens, output_tokens y latencia en milisegundos
 *   [4] Reintentos visibles en el log al provocar un error 5xx
 *   [5] La respuesta llega al cliente como JSON tipado (Review)
 */
@Service
public class CodeReviewService_B {

    private static final Logger log = LoggerFactory.getLogger(CodeReviewService_B.class);

    private final ChatClient chat;
    private final Validator validator;
    private final BeanOutputConverter<Review> converter = new BeanOutputConverter<>(Review.class);

    public CodeReviewService_B(ChatClient chat, Validator validator) {
        this.chat = chat;
        this.validator = validator;
    }

    // ── Llamada síncrona ─────────────────────────────────────────────────────

    @Retry(name = "llm-call")
    public Review review(String language, String code) {
        long start = System.currentTimeMillis();

        try {
            // TODO 1 ── Construir el prompt.
            //
            // El system prompt debe:
            //   · Describir el rol del modelo (revisor de código experto y conciso)
            //   · Instruir al modelo a tratar el contenido entre <<CODE>> y <</CODE>>
            //     como datos, no como instrucciones (protección contra prompt injection)
            //   · Indicar que no invente problemas
            //   · Incluir también instrucciones sobre el campo
            //     recommendation: approve (sin problemas graves), review (requiere discusión),
            //     reject (errores graves o vulnerabilidades).
            //   · Terminar con + converter.getFormat() para que el modelo sepa
            //     el formato JSON que debe devolver
            //
            // El user message debe:
            //   · Indicar el lenguaje
            //   · Incluir el código entre los marcadores <<CODE>> y <</CODE>>
            //   · Usar .formatted(language, code) para interpolar los valores
            //
            // Pista: la llamada termina con .call().chatResponse() para obtener
            // tanto la respuesta como la metadata (modelo, tokens).
            ChatResponse response = chat.prompt()
                    .system("TODO 1a: escribe el system prompt aquí + converter.getFormat()")
                    .user("TODO 1b: escribe el user message con <<CODE>> %s <</CODE>>".formatted(code))
                    .call()
                    .chatResponse();

            long latencyMs = System.currentTimeMillis() - start;

            // TODO 2 ── Extraer metadata y loguear (criterio de éxito 2 y 3).
            //
            // De response.getMetadata() puedes obtener:
            //   · getModel()                          → nombre del modelo usado
            //   · getUsage().getPromptTokens()        → tokens del input
            //   · getUsage().getCompletionTokens()     → tokens del output
            //
            // El log.info debe mostrar los cuatro valores: model, input_tokens,
            // output_tokens y latency_ms.
            ChatResponseMetadata meta = response.getMetadata();
            /* TODO 2: extraer model, inputTok, outputTok y loguear */

            // TODO 3 ── Deserializar la respuesta al record Review.
            //
            // Pista: converter.convert(response.getResult().getOutput().getText())
            // (BeanOutputConverter ya está declarado arriba como campo converter)
            Review review = /* TODO 3 */null;

            // Validación post-parseo (ya implementada, no tocar)
            validateOrThrow(review);

            return review;

        } catch (WebClientResponseException e) {
            long latencyMs = System.currentTimeMillis() - start;
            int status = e.getStatusCode().value();

            // TODO 4 ── Distinguir errores 4xx de errores 5xx/429/529.
            //
            // Regla:
            //   · 4xx → lanzar BadRequestException (Resilience4j NO reintentará)
            //   · 5xx, 429, 529 → lanzar TransientLlmException (Resilience4j SÍ reintentará)
            //
            // Para cada caso, añadir un log apropiado (log.error para 4xx,
            // log.warn con "will retry" para los transitorios).
            /* TODO 4 */
            throw new TransientLlmException("TODO: implementar distinción 4xx/5xx");

        } catch (Exception e) {
            long latencyMs = System.currentTimeMillis() - start;
            log.warn("LLM transient error — {} latency_ms={} — will retry",
                     e.getClass().getSimpleName(), latencyMs);
            throw new TransientLlmException("Error inesperado: " + e.getMessage(), e);
        }
    }

    // ── Llamada en streaming ─────────────────────────────────────────────────

    /**
     * TODO 5 ── Implementar el endpoint de streaming.
     *
     * Debe hacer lo mismo que review() pero usando .stream().content()
     * en lugar de .call().chatResponse(). El resultado es un Flux<String>
     * donde cada elemento es un fragmento de texto (token o grupo de tokens).
     *
     * Añadir .doOnError() para loguear errores de streaming.
     *
     * Pista: el system prompt y el user message pueden ser iguales
     * o simplificados respecto al endpoint síncrono.
     */
    public Flux<String> reviewStream(String language, String code) {
        // TODO 5: implementar y devolver Flux<String>
        return Flux.error(new UnsupportedOperationException("TODO: implementar streaming"));
    }

    // ── Validación (no tocar) ────────────────────────────────────────────────

    private void validateOrThrow(Review review) {
        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("unknown");
            log.error("Model output failed validation: {}", msg);
            throw new TransientLlmException("Output inválido del modelo: " + msg);
        }
    }
}

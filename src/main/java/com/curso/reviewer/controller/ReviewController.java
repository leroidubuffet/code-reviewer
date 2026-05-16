package com.curso.reviewer.controller;

import com.curso.reviewer.model.Review;
import com.curso.reviewer.model.ReviewRequest;
import com.curso.reviewer.service.CodeReviewService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Expone dos endpoints:
 *
 * <ul>
 *   <li>POST /review          — respuesta síncrona con JSON tipado</li>
 *   <li>POST /review/stream   — respuesta en streaming SSE</li>
 * </ul>
 *
 * <p>Igual en la opción A (completa) y en la opción B (TODOs).
 * Toda la lógica está en CodeReviewService.
 */
@RestController
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);
    private final CodeReviewService service;

    public ReviewController(CodeReviewService service) {
        this.service = service;
    }

    /**
     * POST /review
     * Espera a que el modelo termine de generar y devuelve el JSON completo.
     */
    @PostMapping("/review")
    public Review review(@Valid @RequestBody ReviewRequest req) {
        log.debug("POST /review — language={}, codeLength={}",
                  req.language(), req.code().length());
        return service.review(req.language(), req.code());
    }

    /**
     * POST /review/stream
     * Devuelve la respuesta token a token como Server-Sent Events.
     * El cliente ve texto aparecer en tiempo real sin esperar al final.
     */
    @PostMapping(value = "/review/stream",
                 produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> reviewStream(@Valid @RequestBody ReviewRequest req) {
        log.debug("POST /review/stream — language={}, codeLength={}",
                  req.language(), req.code().length());
        return service.reviewStream(req.language(), req.code());
    }
}

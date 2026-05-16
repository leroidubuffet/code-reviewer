package com.curso.reviewer.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Construye el ChatClient a partir del builder que Spring AI autoconfigura.
 *
 * <p>El builder ya tiene el modelo, temperatura y max_tokens de application.yml.
 * Aquí no hace falta repetirlos.
 *
 * <p>Igual en la opción A (completa) y en la opción B (TODOs).
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}

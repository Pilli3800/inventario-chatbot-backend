package com.pilli3800.inventario.config;

import com.pilli3800.inventario.service.ChatPromptService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatClientConfig {

    @Bean
    @Primary
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatPromptService chatPromptService) {
        return chatClientBuilder
                .defaultSystem(chatPromptService.obtenerPromptBase())
                .build();
    }

    @Bean("classifierChatClient")
    public ChatClient classifierChatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }
}

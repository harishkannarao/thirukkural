package com.harishkannarao.thirukkural.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OllamaConfiguration {

	@Bean
	@ConditionalOnProperty(name = "app.ai.chat.provider", havingValue = "ollama")
	@Primary
	public ChatModel defaultChatModel(
		OllamaApi ollamaApi,
		@Value("${app.ai.chat.model}") String ollamaChatModel
	) {
		return OllamaChatModel.builder()
			.ollamaApi(ollamaApi)
			.defaultOptions(
				OllamaOptions.builder()
					.model(ollamaChatModel)
					.temperature(0.9)
					.build()
			)
			.build();
	}
}

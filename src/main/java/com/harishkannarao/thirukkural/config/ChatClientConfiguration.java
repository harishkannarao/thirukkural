package com.harishkannarao.thirukkural.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatClientConfiguration {

	@Bean
	public ChatClient defaultChatClient(ChatModel chatModel) {
		return ChatClient.builder(chatModel)
			.defaultAdvisors(List.of(new SimpleLoggerAdvisor()))
			.defaultSystem("You are a helpful AI Assistant answering questions")
			.build();
	}
}

package com.harishkannarao.thirukkural.translate;

import com.harishkannarao.thirukkural.model.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TranslationService {
    private final ChatClient chatClient;
    private final ClassPathResource translateSystemTemplate = new ClassPathResource(
            "/prompts/translate-system-template.st");
    private final ClassPathResource translateUserTemplate = new ClassPathResource(
            "/prompts/translate-user-template.st");
    private final ClassPathResource transliterateSystemTemplate = new ClassPathResource(
            "/prompts/transliterate-system-template.st");
    private final ClassPathResource transliterateUserTemplate = new ClassPathResource(
            "/prompts/transliterate-user-template.st");
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ConcurrentHashMap<CacheKey, String> transliterateCache = new ConcurrentHashMap<>();

    @Autowired
    public TranslationService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String translate(String sourceLang, String targetLang, String text) {
        log.info("Translating Input {}", text);
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(translateSystemTemplate);
        String systemMessage = systemPromptTemplate.createMessage().getText();
        PromptTemplate promptTemplate = new PromptTemplate(translateUserTemplate);
        Message userMessage = promptTemplate.createMessage(
                Map.of("text", Objects.requireNonNull(text), "source_lang", sourceLang, "target_lang", targetLang));
        Prompt prompt = new Prompt(userMessage);
        String translatedText = chatClient
                .prompt(prompt)
                .system(systemMessage)
                .call()
                .content();
        log.info("Translated Input {} Output {}", text, translatedText);
        return translatedText;
    }

    public String transliterate(String sourceLang, String targetLang, String text) {
        log.info("Transliterating Input {}", text);
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(transliterateSystemTemplate);
        String systemMessage = systemPromptTemplate.createMessage().getText();
        PromptTemplate promptTemplate = new PromptTemplate(transliterateUserTemplate);
        Message userMessage = promptTemplate.createMessage(
                Map.of("text", Objects.requireNonNull(text), "source_lang", sourceLang, "target_lang", targetLang));
        Prompt prompt = new Prompt(userMessage);
        String translatedText = chatClient
                .prompt(prompt)
                .system(systemMessage)
                .call()
                .content();
        log.info("Transliterated Input {} Output {}", text, translatedText);
        return translatedText;
    }

    public String transliterateWithCache(String sourceLang, String targetLang, String text) {
        return transliterateCache.computeIfAbsent(
                new CacheKey(sourceLang, targetLang, text),
                key -> transliterate(key.sourceLang(), key.targetLang(), key.text()));
    }
}

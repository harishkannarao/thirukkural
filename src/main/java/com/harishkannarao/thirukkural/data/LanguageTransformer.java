package com.harishkannarao.thirukkural.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harishkannarao.thirukkural.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class LanguageTransformer {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper objectMapper;
    private final String inputJsonPath;
    private final String outputJsonPath;
    private final String sourceLanguage;
    private final String targetLanguage;

    public LanguageTransformer(
            ObjectMapper objectMapper,
            @Value("${input.json}") String inputJsonPath,
            @Value("${output.json}") String outputJsonPath,
            @Value("${source.language}") String sourceLanguage,
            @Value("${target.language}") String targetLanguage) {
        this.objectMapper = objectMapper;
        this.inputJsonPath = inputJsonPath;
        this.outputJsonPath = outputJsonPath;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
    }

    public void transform() {
        log.info("Transforming from {} to {}", sourceLanguage, targetLanguage);
        try {
            String inputJson = Files.readString(Paths.get(inputJsonPath));
            Book inputBook = objectMapper.readValue(inputJson, Book.class);

            String outputJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputBook);
            Files.writeString(Paths.get(outputJsonPath), outputJson);
            log.info("Transformed from {} to {}", sourceLanguage, targetLanguage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

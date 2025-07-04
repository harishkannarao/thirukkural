package com.harishkannarao.thirukkural.epub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harishkannarao.thirukkural.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Component
@ConditionalOnProperty(name = "task", havingValue = "create_book")
public class EpubCreator {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper objectMapper;
    private final String baseJson;
    private final String otherLanguages;
    private final String outputFile;

    @Autowired
    public EpubCreator(
            ObjectMapper objectMapper,
            @Value("${base.json}") String baseJson,
            @Value("${other.language.jsons}") String otherLanguages,
            @Value("${output.file}") String outputFile) {
        this.objectMapper = objectMapper;
        this.baseJson = baseJson;
        this.otherLanguages = otherLanguages;
        this.outputFile = outputFile;
    }

    public void createBook() {
        log.info("Creating EPUB book");
        log.info("Base {}", baseJson);
        log.info("Other languages {}", otherLanguages);
        Book base = readJsonBook(baseJson);
        List<Book> otherLanguages = Arrays.stream(this.otherLanguages.split(","))
                .map(this::readJsonBook)
                .toList();

    }

    private Book readJsonBook(String file) {
        try {
            String json = Files.readString(Paths.get(file));
            return objectMapper.readValue(json, Book.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

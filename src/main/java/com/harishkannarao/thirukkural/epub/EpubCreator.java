package com.harishkannarao.thirukkural.epub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

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
    }
}

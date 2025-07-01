package com.harishkannarao.thirukkural.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harishkannarao.thirukkural.model.Book;
import com.harishkannarao.thirukkural.model.Chapter;
import com.harishkannarao.thirukkural.model.Couplet;
import com.harishkannarao.thirukkural.model.Volume;
import com.harishkannarao.thirukkural.translate.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class LanguageTransformer {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper objectMapper;
    private final TranslationService translationService;
    private final String inputJsonPath;
    private final String outputJsonPath;
    private final String sourceLanguage;
    private final String targetLanguage;

    public LanguageTransformer(
            ObjectMapper objectMapper,
            TranslationService translationService,
            @Value("${input.json}") String inputJsonPath,
            @Value("${output.json}") String outputJsonPath,
            @Value("${source.language}") String sourceLanguage,
            @Value("${target.language}") String targetLanguage) {
        this.objectMapper = objectMapper;
        this.translationService = translationService;
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

            String name = inputBook.name();
            String transliteratedName = translationService.transliterateWithCache(sourceLanguage, targetLanguage, name);

            List<Volume> volumes = inputBook.volumes()
                    .stream()
                    .limit(1)
                    .map(volume -> {
                        String paulName = volume.paulName();
                        String transliteratedPaul = translationService.transliterateWithCache(sourceLanguage, targetLanguage, paulName);
                        String translatedPaul = translationService.translateWithCache(sourceLanguage, targetLanguage, paulName);
                        List<Chapter> chapters = volume.chapters().stream()
                                .limit(2)
                                .map(chapter -> {
                                    String iyalName = chapter.iyalName();
                                    String adikaramName = chapter.adikaramName();
                                    String translatedIyalName = translationService.translateWithCache(sourceLanguage, targetLanguage, iyalName);
                                    String translatedAdikaramName = translationService.translateWithCache(sourceLanguage, targetLanguage, adikaramName);
                                    String transliteratedIyalName = translationService.transliterateWithCache(sourceLanguage, targetLanguage, iyalName);
                                    String transliteratedAdikaramName = translationService.transliterateWithCache(sourceLanguage, targetLanguage, adikaramName);
                                    List<Couplet> kurals = chapter.kurals().stream()
                                            .limit(2)
                                            .map(couplet -> {
                                                String line1 = couplet.line1();
                                                String line2 = couplet.line2();
                                                String description = couplet.description();
                                                String transliteratedLine1 = translationService.transliterate(sourceLanguage, targetLanguage, line1);
                                                String transliteratedLine2 = translationService.transliterate(sourceLanguage, targetLanguage, line2);
                                                String translated = translationService.translate(sourceLanguage, targetLanguage, description);
                                                return new Couplet(
                                                        couplet.number(),
                                                        line1,
                                                        line2,
                                                        transliteratedLine1,
                                                        transliteratedLine2,
                                                        description,
                                                        translated
                                                );
                                            })
                                            .toList();
                                    return new Chapter(
                                            chapter.number(),
                                            adikaramName,
                                            transliteratedAdikaramName,
                                            translatedAdikaramName,
                                            paulName,
                                            transliteratedPaul,
                                            translatedPaul,
                                            iyalName,
                                            transliteratedIyalName,
                                            translatedIyalName,
                                            kurals
                                    );
                                })
                                .toList();
                        return new Volume(volume.number(), paulName, transliteratedPaul, translatedPaul, chapters);
                    })
                    .toList();
            Book outpuBook = new Book(inputBook.name(), transliteratedName, volumes);

            String outputJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(outpuBook);
            Files.writeString(Paths.get(outputJsonPath), outputJson);
            log.info("Transformed from {} to {}", sourceLanguage, targetLanguage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

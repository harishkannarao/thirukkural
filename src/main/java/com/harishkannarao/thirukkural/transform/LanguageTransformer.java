package com.harishkannarao.thirukkural.transform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harishkannarao.thirukkural.model.Book;
import com.harishkannarao.thirukkural.model.Chapter;
import com.harishkannarao.thirukkural.model.Couplet;
import com.harishkannarao.thirukkural.model.Volume;
import com.harishkannarao.thirukkural.translate.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
@ConditionalOnProperty(name = "task", havingValue = "transform_language")
public class LanguageTransformer {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper objectMapper;
    private final TranslationService translationService;
    private final String inputJsonPath;
    private final String outputJsonPath;
    private final String tamil = "Tamil";
    private final String english = "English";
    private final String targetLanguage;
    private final boolean dryRun;

    public LanguageTransformer(
            ObjectMapper objectMapper,
            TranslationService translationService,
            @Value("${input.json}") String inputJsonPath,
            @Value("${output.json}") String outputJsonPath,
            @Value("${target.language}") String targetLanguage,
            @Value("${dry.run:false}") boolean dryRun) {
        this.objectMapper = objectMapper;
        this.translationService = translationService;
        this.inputJsonPath = inputJsonPath;
        this.outputJsonPath = outputJsonPath;
        this.targetLanguage = targetLanguage;
        this.dryRun = dryRun;
    }

    public void transformBook() {
        log.info("Transforming from {} to {}", tamil, targetLanguage);
        try {
            String inputJson = Files.readString(Paths.get(inputJsonPath));
            Book inputBook = objectMapper.readValue(inputJson, Book.class);

            String transliteratedName = translationService
                    .transliterateWithCache(english, targetLanguage, inputBook.transliteration());
            long limit = dryRun ? 1 : inputBook.volumes().size();

            List<Volume> volumes = inputBook.volumes()
                    .stream()
                    .limit(limit)
                    .map(this::transformVolume)
                    .toList();
            Book outpuBook = new Book(inputBook.name(), transliteratedName, volumes);

            String outputJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(outpuBook);
            Files.writeString(Paths.get(outputJsonPath), outputJson);
            log.info("Transformed from {} to {}", tamil, targetLanguage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Volume transformVolume(Volume volume) {
        log.info("Transforming volume {}", volume.number());
        String paulName = volume.paulName();
        String englishPaulName = volume.paulTranslation();
        String transliteratedPaul = translationService.transliterateWithCache(tamil, targetLanguage, paulName);
        String translatedPaul = translationService.translateWithCache(english, targetLanguage, englishPaulName);
        long limit = dryRun ? 1 : volume.chapters().size();
        List<Chapter> chapters = volume.chapters().stream()
                .limit(limit)
                .map(chapter -> transformChapter(chapter, paulName, transliteratedPaul, translatedPaul))
                .toList();
        return new Volume(volume.number(), paulName, transliteratedPaul, translatedPaul, chapters);
    }

    private Chapter transformChapter(Chapter chapter, String paulName, String transliteratedPaul, String translatedPaul) {
        log.info("Transforming chapter {}", chapter.number());
        String iyalName = chapter.iyalName();
        String adikaramName = chapter.adikaramName();
        String translatedIyalName = translationService.translateWithCache(english, targetLanguage, chapter.iyalTranslation());
        String translatedAdikaramName = translationService.translateWithCache(english, targetLanguage, chapter.adikaramTranslation());
        String transliteratedIyalName = translationService.transliterateWithCache(tamil, targetLanguage, iyalName);
        String transliteratedAdikaramName = translationService.transliterateWithCache(tamil, targetLanguage, adikaramName);
        long limit = dryRun ? 2 : chapter.kurals().size();
        List<Couplet> kurals = chapter.kurals().stream()
                .limit(limit)
                .map(this::transformKural)
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
    }

    private Couplet transformKural(Couplet couplet) {
        log.info("Transforming kural {}", couplet.number());
        String line1 = couplet.line1();
        String line2 = couplet.line2();
        String description = couplet.description();
        String transliteratedLine1 = translationService.transliterate(tamil, targetLanguage, line1);
        String transliteratedLine2 = translationService.transliterate(tamil, targetLanguage, line2);
        String translated = translationService.translate(tamil, targetLanguage, description);
        return new Couplet(
                couplet.number(),
                line1,
                line2,
                transliteratedLine1,
                transliteratedLine2,
                description,
                translated
        );
    }
}

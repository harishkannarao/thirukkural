package com.harishkannarao.thirukkural.epub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harishkannarao.thirukkural.model.Book;
import com.harishkannarao.thirukkural.model.BookMap;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.GuideReference;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        List<BookMap> otherLanguages = Arrays.stream(this.otherLanguages.split(","))
                .map(this::readJsonBook)
                .map(BookMap::new)
                .toList();

        nl.siegmann.epublib.domain.Book book = new nl.siegmann.epublib.domain.Book();
        Metadata metadata = book.getMetadata();
        String title = getTitle(base, otherLanguages);
        String author = "https://github.com/harishkannarao/thirukkural";
        metadata.addTitle(title);
        metadata.addAuthor(new Author(author));
        Resource titleResource = new Resource(createTitle(title, author).getBytes(StandardCharsets.UTF_8), "title.html");
        book.addSection("Title", titleResource);
        book.getGuide().addReference(new GuideReference(titleResource, GuideReference.TITLE_PAGE, "Title"));

        base.volumes().forEach(volume -> {
            Resource volumeResource = new Resource(createVolume(volume.number(), volume.paulName(), otherLanguages).getBytes(StandardCharsets.UTF_8), "vol-%s.html".formatted(volume.number()));
            book.addSection( "vol-%s".formatted(volume.number()), volumeResource);
            book.getGuide().addReference(new GuideReference(volumeResource, GuideReference.TOC, "vol-%s".formatted(volume.number())));
        });

        saveBook(book);
    }

    private Book readJsonBook(String file) {
        try {
            String json = Files.readString(Paths.get(file));
            return objectMapper.readValue(json, Book.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getTitle(Book book, List<BookMap> bookMaps) {
        String otherTitles = bookMaps.stream()
                .map(bookMap -> bookMap.book().transliteration())
                .collect(Collectors.joining(" / "));
        return book.name() + " / " + otherTitles;
    }

    private String createTitle(String title, String author) {
        var generatedDateTime = OffsetDateTime.now(ZoneOffset.UTC)
                .truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_DATE_TIME);
        return String.format("""
                    <html xmlns="http://www.w3.org/1999/xhtml">
                        <head>
                            <title>%s</title>
                        </head>
                        <body>
                            <div style="text-align:center;">
                                <h2>%s</h2>
                                <h4>%s</h4>
                                <h5>Generated Date: %s</h5>
                            </div>
                        </body>
                    </html>
                """, title, title, author, generatedDateTime);
    }

    private String createVolume(Integer volumeNumber, String baseName, List<BookMap> bookMaps) {
        String baseText = """
                <span style="text-align:center;">
                                <h2>%s</h2>
                            </span>
                """
                .formatted(baseName);
        String otherVolumeNames = bookMaps.stream().sequential()
                .map(bookMap -> {
                    String paulTransliteration = bookMap.volumeMap().get(volumeNumber).paulTransliteration();
                    String paulTranslation = bookMap.volumeMap().get(volumeNumber).paulTranslation();
                    return """
                            <span style="text-align:center;">
                                <h2>%s<br/>(%s)</h2>
                            </span>
                            """
                            .formatted(paulTransliteration, paulTranslation);
                })
                .collect(Collectors.joining(System.lineSeparator()));
        return String.format("""
                    <html xmlns="http://www.w3.org/1999/xhtml">
                        <head>
                            <title>%s</title>
                        </head>
                        <body>
                            <span style="text-align:center;">
                                <h2>%s</h2>
                            </span>
                            %s
                            <br/>
                            %s
                        </body>
                    </html>
                """, volumeNumber, volumeNumber, baseText, otherVolumeNames);
    }

    private void saveBook(nl.siegmann.epublib.domain.Book book) {
        var epubWriter = new EpubWriter();
        var file = Paths.get(outputFile).toFile();
        try {
            epubWriter.write(book, new FileOutputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.harishkannarao.thirukkural.epub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harishkannarao.thirukkural.model.*;
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
import java.util.stream.Stream;

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

        addTitle(book, base, otherLanguages);
        addVolumes(book, base, otherLanguages);

        saveBook(book);
    }

    private void addVolumes(nl.siegmann.epublib.domain.Book book, Book base, List<BookMap> otherLanguages) {
        Resource volumeSummaryResource = new Resource(createVolumeSummary(base, otherLanguages).getBytes(StandardCharsets.UTF_8), "volume-summary.html");
        book.addSection("volume-summary", volumeSummaryResource);

        base.volumes().forEach(volume -> {
            Resource volumeResource = new Resource(createVolume(volume.number(), volume.paulName(), otherLanguages).getBytes(StandardCharsets.UTF_8), "volume-%s.html".formatted(volume.number()));
            book.addSection("volume-%s".formatted(volume.number()), volumeResource);
            book.getGuide().addReference(new GuideReference(volumeResource, GuideReference.TOC, "volume-%s".formatted(volume.number())));

            Resource volumeChaptersResource = new Resource(createVolumeChapters(volume, otherLanguages).getBytes(StandardCharsets.UTF_8), "volume-%s-chapters.html".formatted(volume.number()));
            book.addSection("volume-%s-chapters".formatted(volume.number()), volumeChaptersResource);

            addChapters(book, volume, otherLanguages);
        });
    }

    private void addChapters(nl.siegmann.epublib.domain.Book book, Volume volume, List<BookMap> otherLanguages) {
        volume.chapters().forEach(chapter -> {
            Resource chapterResource = new Resource(createChapter(volume.number(), chapter, otherLanguages).getBytes(StandardCharsets.UTF_8), "chapter-%s.html".formatted(chapter.number()));
            book.addSection("chapter-%s".formatted(chapter.number()), chapterResource);
            addCouplets(book, chapter, otherLanguages);
        });
    }

    private void addCouplets(nl.siegmann.epublib.domain.Book book, Chapter chap, List<BookMap> otherLanguages) {
        chap.kurals().forEach(kural -> {
            Resource kuralResource = new Resource(createKural(chap.number(), kural, otherLanguages).getBytes(StandardCharsets.UTF_8), "kural-%s.html".formatted(kural.number()));
            book.addSection("kural-%s".formatted(kural.number()), kuralResource);
        });
    }

    private String createKural(int chapterNumber, Couplet kural, List<BookMap> otherLanguages) {
        String baseText = """
                <span style="text-align:center;">
                    <h4>%s<br/>%s</h4>
                    <h4>%s</h4>
                </span>
                """
                .formatted(kural.line1(), kural.line2(), kural.description());
        String otherTexts = otherLanguages.stream()
                .map(bookMap -> {
                    String line1 = bookMap.coupletMap().get(kural.number()).line1TransLiteration();
                    String line2 = bookMap.coupletMap().get(kural.number()).line2TransLiteration();
                    String description = bookMap.coupletMap().get(kural.number()).translation();
                    return """
                            <span style="text-align:center;">
                                <h4>%s<br/>%s</h4>
                                <h4>%s</h4>
                            </span>
                            """
                            .formatted(line1, line2, description);
                })
                .collect(Collectors.joining(System.lineSeparator()));
        return String.format("""
                    <html xmlns="http://www.w3.org/1999/xhtml">
                        <body>
                            <span style="text-align:center;">
                                <h2>%s</h2>
                            </span>
                            %s
                            %s
                            <br/>
                            <br/>
                            <span style="text-align:center;">
                                <h2><a href="chapter-%s.html">UP</a></h2>
                            </span>
                        </body>
                    </html>
                """, kural.number(), baseText, otherTexts, chapterNumber);
    }


    private String createChapter(int volumeNumber, Chapter chapter, List<BookMap> otherLanguages) {
        String baseText = """
                <span style="text-align:center;">
                                <h2>%s</h2>
                                <h4>%s</h4>
                            </span>
                """
                .formatted(chapter.adikaramName(), chapter.iyalName());
        String otherChapterNames = otherLanguages.stream()
                .map(bookMap -> {
                    String adikaramTransliteration = bookMap.chapterMap().get(chapter.number()).adikaramTransliteration();
                    String adikaramTranslation = bookMap.chapterMap().get(chapter.number()).adikaramTranslation();
                    String iyalTransliteration = bookMap.chapterMap().get(chapter.number()).iyalTransliteration();
                    String iyalTranslation = bookMap.chapterMap().get(chapter.number()).iyalTranslation();
                    return """
                            <span style="text-align:center;">
                                <h2>%s ( %s )</h2>
                                <h4>%s ( %s )</h4>
                            </span>
                            """
                            .formatted(adikaramTransliteration, adikaramTranslation, iyalTransliteration, iyalTranslation);
                })
                .collect(Collectors.joining(System.lineSeparator()));
        return String.format("""
                    <html xmlns="http://www.w3.org/1999/xhtml">
                        <body>
                            <span style="text-align:center;">
                                <h2>%s</h2>
                            </span>
                            %s
                            %s
                            <br/>
                            <br/>
                            <span style="text-align:center;">
                                <h2><a href="volume-%s.html">UP</a></h2>
                            </span>
                        </body>
                    </html>
                """, chapter.number(), baseText, otherChapterNames, volumeNumber);
    }

    private void addTitle(nl.siegmann.epublib.domain.Book book, Book base, List<BookMap> otherLanguages) {
        Metadata metadata = book.getMetadata();
        String title = getTitle(base, otherLanguages);
        String author = "https://github.com/harishkannarao/thirukkural";
        metadata.addTitle(title);
        metadata.addAuthor(new Author(author));
        Resource titleResource = new Resource(createTitle(title, author).getBytes(StandardCharsets.UTF_8), "title.html");
        book.addSection("Title", titleResource);
        book.getGuide().addReference(new GuideReference(titleResource, GuideReference.TITLE_PAGE, "Title"));
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
                                <h4><a href="%s">%s</a></h4>
                                <h5>Generated Date: %s</h5>
                            </div>
                        </body>
                    </html>
                """, title, title, author, author, generatedDateTime);
    }

    private String createVolumeSummary(Book book, List<BookMap> bookMaps) {
        String volumesSummary = book.volumes().stream()
                .map(volume -> {
                    int vNumber = volume.number();
                    String vName = volume.paulName();
                    List<String> otherNames = bookMaps.stream()
                            .map(bookMap -> bookMap.volumeMap().get(vNumber))
                            .map(Volume::paulTranslation)
                            .toList();
                    String volumeNames = Stream.of(Stream.of(String.valueOf(vNumber), vName), otherNames.stream())
                            .flatMap(it -> it)
                            .collect(Collectors.joining(" / "));
                    return """
                            <span style="text-align:center;">
                                            <a href="volume-%s.html"><h2>%s</h2></a>
                                        </span>
                            """.formatted(vNumber, volumeNames);
                })
                .collect(Collectors.joining("<br/><br/>"));
        return String.format("""
                    <html xmlns="http://www.w3.org/1999/xhtml">
                        <body>
                            %s
                        </body>
                    </html>
                """, volumesSummary);
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
                            %s
                            <br/>
                            <br/>
                            <span style="text-align:center;">
                                <h2><a href="volume-summary.html">UP</a></h2>
                            </span>
                        </body>
                    </html>
                """, volumeNumber, volumeNumber, baseText, otherVolumeNames);
    }

    private String createVolumeChapters(Volume volume, List<BookMap> bookMaps) {
        String chapters = volume.chapters().stream()
                .map(chapter -> {
                    int cNumber = chapter.number();
                    String baseName = chapter.adikaramName();
                    String otherNames = bookMaps.stream()
                            .map(bookMap -> bookMap.chapterMap().get(cNumber))
                            .map(Chapter::adikaramTranslation)
                            .collect(Collectors.joining(" / "));
                    String chapterName = cNumber + " / " + baseName + " / " + otherNames;
                    return """
                            <span style="text-align:center;">
                                <h2><a href="chapter-%s.html">%s</a></h2>
                            </span>
                            """
                            .formatted(chapter.number(), chapterName);
                })
                .collect(Collectors.joining("<br/><br/>"));
        return String.format("""
                    <html xmlns="http://www.w3.org/1999/xhtml">
                        <body>
                            %s
                        </body>
                    </html>
                """, chapters);
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

package com.harishkannarao.thirukkural.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harishkannarao.thirukkural.model.Book;
import com.harishkannarao.thirukkural.model.Chapter;
import com.harishkannarao.thirukkural.model.Couplet;
import com.harishkannarao.thirukkural.model.Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class JsonStructureTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(JsonStructureTransformer.class);

    private final ObjectMapper objectMapper;
    private final String inputJsonPath;
    private final String outputJsonPath;

    @Autowired
    public JsonStructureTransformer(
            ObjectMapper objectMapper,
            @Value("${input.json}") String inputJsonPath,
            @Value("${output.json}") String outputJsonPath) {
        this.objectMapper = objectMapper;
        this.inputJsonPath = inputJsonPath;
        this.outputJsonPath = outputJsonPath;
    }

    public void transform() {
        LOG.info("Transforming raw json to structured json");
        try {
            String inputJson = Files.readString(Paths.get(inputJsonPath));
            JsonNode rootNode = objectMapper.readTree(inputJson);
            if (rootNode.isArray()) {
                Map<String, List<JsonNode>> groupedByChapters = StreamSupport.stream(rootNode.spliterator(), false)
                        .collect(Collectors.groupingBy(element -> element.get("adikaram_transliteration").asText()));

                List<Chapter> chapters = groupedByChapters.values().stream()
                        .map(jsonNodes -> {
                            JsonNode aKural = jsonNodes.getFirst();
                            int chapterNumber = (aKural.get("Number").asInt() / 10) + 1;
                            List<Couplet> couplets = jsonNodes.stream()
                                    .map(node -> new Couplet(
                                            node.get("Number").asInt(),
                                            node.get("Line1").asText(),
                                            node.get("Line2").asText(),
                                            node.get("transliteration1").asText(),
                                            node.get("transliteration2").asText(),
                                            node.get("mv").asText(),
                                            node.get("explanation").asText()
                                    ))
                                    .sorted(Comparator.comparingInt(Couplet::number))
                                    .toList();
                            return new Chapter(
                                    chapterNumber,
                                    aKural.get("adikaram_name").asText(),
                                    aKural.get("adikaram_transliteration").asText(),
                                    aKural.get("adikaram_translation").asText(),
                                    aKural.get("paul_name").asText(),
                                    aKural.get("paul_transliteration").asText(),
                                    aKural.get("paul_translation").asText(),
                                    aKural.get("iyal_name").asText(),
                                    aKural.get("iyal_transliteration").asText(),
                                    aKural.get("iyal_translation").asText(),
                                    couplets);
                        })
                        .sorted(Comparator.comparingInt(Chapter::number))
                        .toList();
                Map<String, List<Chapter>> groupedByVolumes = chapters.stream()
                        .collect(Collectors.groupingBy(Chapter::paulTransliteration));

                List<Volume> volumes = groupedByVolumes.values().stream()
                        .map(chapterList -> {
                            Chapter aChapter = chapterList.getFirst();
                            int volumeNumber = 0;
                            if (aChapter.number() >= 1 && aChapter.number() <= 38) {
                                volumeNumber = 1;
                            } else if (aChapter.number() >= 39 && aChapter.number() <= 108) {
                                volumeNumber = 2;
                            } else {
                                volumeNumber = 3;
                            }
                            return new Volume(
                                    volumeNumber,
                                    aChapter.paulName(),
                                    aChapter.paulTransliteration(),
                                    aChapter.paulTranslation(),
                                    chapterList
                            );
                        })
                        .sorted(Comparator.comparingInt(Volume::number))
                        .toList();
                Book book = new Book("திருக்குறள்", "Thirukkural", volumes);
                String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(book);
                Files.writeString(Paths.get(outputJsonPath), jsonString);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOG.info("Transforming to structured json completed");
    }

}

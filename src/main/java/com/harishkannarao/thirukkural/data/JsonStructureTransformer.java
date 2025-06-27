package com.harishkannarao.thirukkural.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
                record Chapter(
                        int number,
                        String adikaramName,
                        String adikaramTransliteration,
                        String adikaramTranslation,
                        String paulName,
                        String paulTransliteration,
                        String paulTranslation,
                        String iyalName,
                        String iyalTransliteration,
                        String iyalTranslation,
                        List<JsonNode> kurals
                ) {

                }
                List<Chapter> chapters = groupedByChapters.values().stream()
                        .map(jsonNodes -> {
                            JsonNode kural = jsonNodes.stream().findAny().orElseThrow();
                            int chapterNumber = (kural.get("Number").asInt() / 10) + 1;
                            return new Chapter(
                                    chapterNumber,
                                    kural.get("adikaram_name").asText(),
                                    kural.get("adikaram_transliteration").asText(),
                                    kural.get("adikaram_translation").asText(),
                                    kural.get("paul_name").asText(),
                                    kural.get("paul_transliteration").asText(),
                                    kural.get("paul_translation").asText(),
                                    kural.get("iyal_name").asText(),
                                    kural.get("iyal_transliteration").asText(),
                                    kural.get("iyal_translation").asText(),
                                    jsonNodes);
                        })
                        .sorted(Comparator.comparingInt(Chapter::number))
                        .toList();
                String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(chapters);
                Files.writeString(Paths.get(outputJsonPath), jsonString);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOG.info("Transforming to structured json completed");
    }

}

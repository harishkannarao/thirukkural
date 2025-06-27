package com.harishkannarao.thirukkural.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
            @Value("${output.json}") String outputJsonPath)

    {
        this.objectMapper = objectMapper;
        this.inputJsonPath = inputJsonPath;
        this.outputJsonPath = outputJsonPath;
    }

    public void transform() {
        LOG.info("Transforming raw json to structured json");
        try {
            String inputJson = Files.readString(Paths.get(inputJsonPath));
            JsonNode jsonNode = objectMapper.readTree(inputJson);
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
            Files.writeString(Paths.get(outputJsonPath), jsonString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOG.info("Transforming to structured json completed");
    }

}

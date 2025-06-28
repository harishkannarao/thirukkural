package com.harishkannarao.thirukkural.model;

import java.util.List;

public record Book(
        String name,
        String transliteration,
        List<Volume> volumes
) {
}

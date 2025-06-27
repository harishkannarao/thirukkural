package com.harishkannarao.thirukkural.model;

public record Couplet(
        int number,
        String line1,
        String line2,
        String line1TransLiteration,
        String line2TransLiteration,
        String description,
        String translation
) {
}

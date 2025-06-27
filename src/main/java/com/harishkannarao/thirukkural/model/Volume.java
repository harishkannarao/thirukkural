package com.harishkannarao.thirukkural.model;

import java.util.List;

public record Volume(
        int number,
        String paulName,
        String paulTransliteration,
        String paulTranslation,
        List<Chapter> chapters
) {
}

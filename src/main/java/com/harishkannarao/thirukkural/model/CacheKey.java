package com.harishkannarao.thirukkural.model;

public record CacheKey(
        String sourceLang,
        String targetLang,
        String text
) {
}

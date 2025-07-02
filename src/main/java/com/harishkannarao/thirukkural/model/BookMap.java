package com.harishkannarao.thirukkural.model;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record BookMap(
        Book book,
        Map<Integer, Volume> volumeMap,
        Map<Integer, Chapter> chapterMap,
        Map<Integer, Couplet> coupletMap
) {
    public BookMap(Book book) {
        this(book, getVolumeMap(book), getChapterMap(book), getCoupletMap(book));
    }

    private static Map<Integer, Couplet> getCoupletMap(Book book) {
        return book.volumes().stream()
                .map(Volume::chapters)
                .flatMap(Collection::stream)
                .map(Chapter::kurals)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableMap(Couplet::number, Function.identity()));
    }

    private static Map<Integer, Chapter> getChapterMap(Book book) {
        return book.volumes().stream()
                .map(Volume::chapters)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableMap(Chapter::number, Function.identity()));
    }

    private static Map<Integer, Volume> getVolumeMap(Book book) {
        return book.volumes().stream()
                .collect(Collectors.toUnmodifiableMap(Volume::number, Function.identity()));
    }
}

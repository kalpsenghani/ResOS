package com.resos.shared.api;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ApiResponse<T> {

    private final T data;
    private final Meta meta;

    public static <T> ApiResponse<T> of(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .meta(Meta.builder().timestamp(Instant.now()).build())
                .build();
    }

    public static <T> ApiResponse<List<T>> page(List<T> data, PageMeta pageMeta) {
        return ApiResponse.<List<T>>builder()
                .data(data)
                .meta(Meta.builder()
                        .timestamp(Instant.now())
                        .page(pageMeta.page())
                        .size(pageMeta.size())
                        .totalElements(pageMeta.totalElements())
                        .totalPages(pageMeta.totalPages())
                        .build())
                .build();
    }

    @Getter
    @Builder
    public static class Meta {
        private final Instant timestamp;
        private final Integer page;
        private final Integer size;
        private final Long totalElements;
        private final Integer totalPages;
    }

    public record PageMeta(int page, int size, long totalElements, int totalPages) {}
}

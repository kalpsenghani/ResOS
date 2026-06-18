package com.resos.shared.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ErrorResponse {

    private final ErrorBody error;

    @Getter
    @Builder
    public static class ErrorBody {
        private final String code;
        private final String message;
        private final List<String> details;
        private final Instant timestamp;
        private final String path;
    }

    public static ErrorResponse of(String code, String message, String path) {
        return ErrorResponse.builder()
                .error(ErrorBody.builder()
                        .code(code)
                        .message(message)
                        .details(List.of())
                        .timestamp(Instant.now())
                        .path(path)
                        .build())
                .build();
    }

    public static ErrorResponse of(String code, String message, List<String> details, String path) {
        return ErrorResponse.builder()
                .error(ErrorBody.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .timestamp(Instant.now())
                        .path(path)
                        .build())
                .build();
    }
}

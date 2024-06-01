package com.example.fasipemobilej.model.response;

import java.util.List;

public record AnamneseListPage(
        List<AnamneseResponse> content,
        int totalPages,
        long totalElements,
        int number,
        int size,
        boolean last,
        boolean first,
        int numberOfElements
) {}

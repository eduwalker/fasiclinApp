package com.example.fasipemobilej.model.response;

import java.util.List;

public record AnamneseAnswerResponse(
        boolean success,
        String message,
        Long anamneseId
) {}

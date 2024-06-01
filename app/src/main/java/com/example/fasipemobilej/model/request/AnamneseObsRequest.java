package com.example.fasipemobilej.model.request;

public record AnamneseObsRequest(
        Long anamneseId,
        String status,
        String observacoes
) {
}

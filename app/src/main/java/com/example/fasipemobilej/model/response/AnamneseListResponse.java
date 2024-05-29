package com.example.fasipemobilej.model.response;

import java.time.LocalDateTime;
import java.util.List;

public record AnamneseListResponse(
        List<AnamneseResponse> anamneses
) {
}

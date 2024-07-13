package com.example.fasipemobilej.model.response;

import java.time.LocalDateTime;

public record AnamneseResponse(
        Long idAnamnese,
        Long codProf,
        String codPac,
        LocalDateTime dataAnamnese,
        String statusAnamnese,
        String statusAnamneseFn,
        String observacoes,
        String nomeProf,
        int authPac,
        PacienteResponse pacienteResponseDTO
) {
}

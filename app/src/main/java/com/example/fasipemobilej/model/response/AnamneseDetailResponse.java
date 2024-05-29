package com.example.fasipemobilej.model.response;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public record AnamneseDetailResponse(
        Long idAnamnese,
        PacienteResponse paciente,
        String statusAnamnese,
        LocalDateTime dataAnamnese,
        List<AnamnePerguntaResposta> perguntasRespostas,
        ProfissionalAnamneseResponse profissional
) {}
package com.example.fasipemobilej.model.request;

import java.util.List;

public record AnamneseAnswerRequest(Long anamneseID, List<RespostaDTO> respostas) {
}

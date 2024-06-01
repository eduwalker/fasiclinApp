package com.example.fasipemobilej.model.response;

public record UserResponse(
        String nome,
        int tipo,
        Long supervisor,
        int status,
        String consProf,
        Long codProf
) {
}

package com.example.fasipemobilej.model.response;

public record UserResponse(
        String nome,
        Long tipo,
        Long supervisor,
        int status,
        String consProf,
        Long codProf
) {
}

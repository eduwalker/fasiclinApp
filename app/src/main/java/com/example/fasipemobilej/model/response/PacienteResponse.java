package com.example.fasipemobilej.model.response;

import java.time.LocalDate;

public record PacienteResponse(
        String cpf_pac,
        String nome_pac,
        Long cod_pac,
        String tel_pac,
        String cep_pac,
        String logra_pac,
        Long num_logra_pac,
        String compl_pac,
        String bairro_pac,
        String cidade_pac,
        String uf_pac,
        String rg_pac,
        String est_rg_pac,
        String nome_mae_pac,
        String data_nasc_pac
) {
}

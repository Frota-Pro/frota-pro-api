package br.com.frotasPro.api.projections;

import java.time.LocalDate;

public interface ClienteHistoricoRotaProjection {
    String getCliente();
    String getCidade();
    Long getQuantidadeCargas();
    LocalDate getUltimaCargaEm();

    // Endereço do Cliente vinculado (tb_cliente), quando já cadastrado —
    // null enquanto a nota ainda não foi enriquecida (ver ClienteService).
    String getDocumento();
    String getLogradouro();
    String getNumero();
    String getComplemento();
    String getBairro();
    String getUf();
    String getCep();
}

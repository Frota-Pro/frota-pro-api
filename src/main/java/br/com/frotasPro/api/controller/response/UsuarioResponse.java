package br.com.frotasPro.api.controller.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponse {

    private UUID id;
    private String login;
    private String nome;
    private boolean ativo;
    private List<String> acessos;

    /** Celular a que o motorista está vinculado (só o nome do modelo; o identificador nunca sai da API). */
    private boolean dispositivoVinculado;
    private String dispositivoVinculadoNome;
    private LocalDateTime dispositivoVinculadoEm;

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;


}

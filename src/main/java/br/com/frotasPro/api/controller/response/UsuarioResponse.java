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

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;


}

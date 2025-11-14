package br.com.frotasPro.api.controller.response;

import lombok.*;

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
    private List<String> acessos;


}

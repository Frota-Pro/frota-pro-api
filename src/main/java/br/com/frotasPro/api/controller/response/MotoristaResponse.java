package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.enums.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MotoristaResponse {

    private UUID id;

    private String codigo;
    private String codigoExterno;

    private String nome;
    private String email;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataNascimento;

    private String cnh;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate validadeCnh;

    private Status status;
    private boolean ativo;
}

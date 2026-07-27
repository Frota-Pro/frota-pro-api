package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotoristaAcessoResponse {

    private String codigoMotorista;
    private String nomeMotorista;

    private LocalDateTime ultimoLoginEm;
    private long totalLogins;
    private Long diasSemAcesso;

    private String dispositivoAppVersao;
    private LocalDateTime dispositivoAppReportadoEm;
}

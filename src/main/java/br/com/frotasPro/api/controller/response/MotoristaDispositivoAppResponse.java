package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.enums.TipoPlataformaDispositivo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MotoristaDispositivoAppResponse {

    private String codigoMotorista;
    private String nomeMotorista;

    private String dispositivoAppVersao;
    private TipoPlataformaDispositivo dispositivoAppPlataforma;
    private LocalDateTime dispositivoAppReportadoEm;

    private String versaoMaisRecenteDisponivel;
    private boolean desatualizado;
}

package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.CargaSyncDescartada;
import br.com.frotasPro.api.domain.enums.MotivoCargaSyncDescartada;
import br.com.frotasPro.api.integracao.dto.CargaWinThorDto;
import br.com.frotasPro.api.repository.CargaSyncDescartadaRepository;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Guarda as cargas que o sync com o WinThor descartou (motorista/caminhão do
 * MDF-e sem cadastro no FrotaPRO) pra aparecerem no relatório, e apaga o
 * registro quando a carga finalmente sincroniza. Roda dentro da transação do
 * sync (não abre transação própria) — se o lote do sync falhar e voltar atrás,
 * o registro volta junto, e na próxima rodada é regravado.
 */
@Service
@RequiredArgsConstructor
public class RegistrarCargaSyncDescartadaService {

    private final CargaSyncDescartadaRepository repository;

    public void registrar(CargaWinThorDto dto, MotivoCargaSyncDescartada motivo) {
        String numeroCargaExterno = String.valueOf(dto.getNumCar());
        var agora = FusoHorarioUtils.agoraBrasil();

        CargaSyncDescartada registro = repository.findByNumeroCargaExterno(numeroCargaExterno)
                .map(existente -> {
                    existente.setTentativas(existente.getTentativas() + 1);
                    return existente;
                })
                .orElseGet(() -> {
                    CargaSyncDescartada novo = new CargaSyncDescartada();
                    novo.setNumeroCargaExterno(numeroCargaExterno);
                    novo.setPrimeiraOcorrenciaEm(agora);
                    return novo;
                });

        // Sempre sobrescreve com o dado mais recente do WinThor: o MDF-e pode ter
        // sido reemitido com outro motorista/veículo desde a última tentativa.
        registro.setMotivo(motivo);
        registro.setNumMdfe(dto.getNumMdfe());
        registro.setSituacaoMdfe(dto.getSituacaoMdfe());
        registro.setCodigoMotoristaExterno(dto.getCodMotorista() != null ? String.valueOf(dto.getCodMotorista()) : null);
        registro.setNomeMotoristaExterno(dto.getNomeMotorista());
        registro.setCodigoVeiculoExterno(dto.getCodVeiculo());
        registro.setPlacaVeiculoExterno(dto.getPlacaVeiculo());
        registro.setDtSaida(dto.getDtSaida() != null ? dto.getDtSaida().toLocalDate() : null);
        registro.setPesoCarga(dto.getPesoTotalKg() != null ? BigDecimal.valueOf(dto.getPesoTotalKg()) : null);
        registro.setValorTotal(dto.getValorTotal());
        registro.setDestino(dto.getDestino());
        registro.setTotalClientes(dto.getTotalClientes());
        registro.setUltimaOcorrenciaEm(agora);

        repository.save(registro);
    }

    public void resolver(Integer numCar) {
        if (numCar != null) {
            repository.deletarPorNumeroCargaExterno(String.valueOf(numCar));
        }
    }
}

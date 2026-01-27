package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.CargaNota;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.integracao.dto.CargaSyncResponseEvent;
import br.com.frotasPro.api.integracao.dto.CargaWinThorDto;
import br.com.frotasPro.api.integracao.dto.ClienteCargaWinThorDto;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CargaNotaRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.repository.RotaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizarCargaService {

    private final CargaRepository cargaRepository;
    private final MotoristaRepository motoristaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final RotaRepository rotaRepository;
    private final CargaNotaRepository cargaNotaRepository;

    @Transactional
    public void sincronizarCargasWinThor(CargaSyncResponseEvent event) {
        log.info("Sincronizando {} cargas da data {} (jobId={})",
                event.getTotalCargas(), event.getDataReferencia(), event.getJobId());

        event.getCargas().forEach(this::upsertCargaFromWinThorWithRetry);
    }

    private void upsertCargaFromWinThorWithRetry(CargaWinThorDto dto) {
        int maxTentativas = 3;

        for (int tentativa = 1; tentativa <= maxTentativas; tentativa++) {
            try {
                upsertCargaFromWinThor(dto);
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                if (tentativa >= maxTentativas) {
                    log.error("Falha por concorrência ao salvar carga/nota após {} tentativas. numCar={} numMdfe={}",
                            tentativa, dto.getNumCar(), dto.getNumMdfe(), e);
                    throw e;
                }

                long backoffMs = 100L * tentativa;
                log.warn("Concorrência detectada ao salvar carga/nota. Tentando novamente ({}/{}). numCar={} backoff={}ms",
                        tentativa, maxTentativas, dto.getNumCar(), backoffMs);

                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
    }

    private void upsertCargaFromWinThor(CargaWinThorDto dto) {

        Carga carga = cargaRepository
                .findByNumeroCargaExterno(dto.getNumCar().toString())
                .orElseGet(Carga::new);

        boolean nova = carga.getId() == null;

        if (nova) {
            carga.setNumeroCargaExterno(dto.getNumCar().toString());
        }

        // Garanta que a coleção existe e esteja consistente
        if (carga.getNotas() == null) {
            carga.setNotas(new ArrayList<>());
        }

        // Evite "delete manual" + mexer na coleção ao mesmo tempo.
        // Deixe o JPA fazer orphanRemoval/cascade (ajuste no mapeamento se necessário).
        carga.getNotas().clear();

        var motoristaOpt = motoristaRepository
                .findByCodigoExterno(String.valueOf(dto.getCodMotorista()));

        if (motoristaOpt.isEmpty()) {
            log.warn("Motorista WinThor {} não encontrado. Ignorando MDF-e {}",
                    dto.getCodMotorista(), dto.getNumMdfe());
            return;
        }
        carga.setMotorista(motoristaOpt.get());

        var caminhaoOpt = caminhaoRepository
                .findByCodigoExterno(String.valueOf(dto.getCodVeiculo()));

        if (caminhaoOpt.isEmpty()) {
            log.warn("Caminhão WinThor {} não encontrado. Ignorando MDF-e {}",
                    dto.getCodVeiculo(), dto.getNumMdfe());
            return;
        }
        carga.setCaminhao(caminhaoOpt.get());

        String destino = dto.getDestino();
        Rota rota = rotaRepository.findByCidadeInicio(destino)
                .orElseGet(() -> {
                    Rota novaRota = new Rota();
                    novaRota.setCidadeInicio(destino);
                    return rotaRepository.save(novaRota);
                });
        carga.setRota(rota);

        if (dto.getDtSaida() != null) {
            carga.setDtFaturamento(dto.getDtSaida().toLocalDate());
        }

        carga.setPesoCarga(dto.getPesoTotalKg() != null
                ? BigDecimal.valueOf(dto.getPesoTotalKg())
                : null);

        carga.setValorTotal(
                dto.getValorTotal() != null && dto.getValorTotal().compareTo(BigDecimal.ZERO) > 0
                        ? dto.getValorTotal()
                        : null
        );

        carga.setStatusCarga(Status.SINCRONIZADA);

        int totalClientes = 0;
        int totalNotas = 0;

        for (ClienteCargaWinThorDto cli : dto.getClientes()) {

            String clienteStr = cli.getCodCli() + " - " + cli.getNomeCli();
            totalClientes++;

            if (cli.getNotas() != null) {
                for (Long nota : cli.getNotas()) {
                    CargaNota cn = new CargaNota();
                    cn.setCarga(carga);
                    cn.setCliente(clienteStr);
                    cn.setNota(String.valueOf(nota));
                    carga.getNotas().add(cn);
                    totalNotas++;
                }
            }
        }

        // Flush ajuda a detectar conflitos cedo e reduz surpresa no commit do listener.
        cargaRepository.saveAndFlush(carga);

        log.info("Carga {} sincronizada. {} clientes, {} notas",
                dto.getNumMdfe(),
                totalClientes,
                totalNotas);
    }

}
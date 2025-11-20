package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.CargaCliente;
import br.com.frotasPro.api.domain.CargaNota;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.integracao.dto.CargaSyncResponseEvent;
import br.com.frotasPro.api.integracao.dto.CargaWinThorDto;
import br.com.frotasPro.api.integracao.dto.ClienteCargaWinThorDto;
import br.com.frotasPro.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizarCargaService {

    private final CargaRepository cargaRepository;
    private final MotoristaRepository motoristaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final RotaRepository rotaRepository;
    private final CargaClienteRepository cargaClienteRepository;
    private final CargaNotaRepository cargaNotaRepository;

    @Transactional
    public void sincronizarCargasWinThor(CargaSyncResponseEvent event) {
        log.info("Sincronizando {} cargas da data {} (jobId={})",
                event.getTotalCargas(), event.getDataReferencia(), event.getJobId());

        event.getCargas().forEach(this::upsertCargaFromWinThor);
    }

    private void upsertCargaFromWinThor(CargaWinThorDto dto) {

        Carga carga = cargaRepository
                .findByNumeroCargaExterno(dto.getNumCar().toString())
                .orElseGet(Carga::new);

        boolean nova = carga.getId() == null;

        if (nova) {
            carga.setNumeroCargaExterno(dto.getNumCar().toString());
        } else {
            cargaClienteRepository.deleteByCargaId(carga.getId());
            cargaNotaRepository.deleteByCargaId(carga.getId());

            carga.getClientes().clear();
            carga.getNotas().clear();
        }

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
        var rota = rotaRepository.findByCidadeInicio(destino)
                .orElseGet(() -> {
                    Rota novaRota = new Rota();
                    novaRota.setCidadeInicio(destino);
                    return rotaRepository.save(novaRota);
                });
        carga.setRota(rota);

        carga.setDtFaturamento(dto.getDtSaida().toLocalDate());

        carga.setPesoCarga(dto.getPesoTotalKg() != null
                ? BigDecimal.valueOf(dto.getPesoTotalKg())
                : null);

        carga.setStatusCarga(Status.SINCRONIZADA);

        for (ClienteCargaWinThorDto cli : dto.getClientes()) {
            CargaCliente cc = new CargaCliente();
            cc.setCarga(carga);
            cc.setCliente(cli.getCodCli() + " - " + cli.getNomeCli());
            carga.getClientes().add(cc);
        }

        Set<String> notas = dto.getClientes().stream()
                .flatMap(c -> c.getNotas().stream())
                .map(String::valueOf)
                .collect(Collectors.toCollection(HashSet::new));

        for (String nota : notas) {
            CargaNota cn = new CargaNota();
            cn.setCarga(carga);
            cn.setNota(nota);
            carga.getNotas().add(cn);
        }

        cargaRepository.save(carga);

        log.info("Carga {} sincronizada. {} clientes, {} notas",
                dto.getNumMdfe(),
                carga.getClientes().size(),
                carga.getNotas().size());
    }

}

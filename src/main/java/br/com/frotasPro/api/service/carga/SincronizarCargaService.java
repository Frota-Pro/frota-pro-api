package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.CargaCliente;
import br.com.frotasPro.api.domain.CargaNota;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.integracao.dto.CargaSyncResponseEvent;
import br.com.frotasPro.api.integracao.dto.CargaWinThorDto;
import br.com.frotasPro.api.integracao.dto.ClienteCargaWinThorDto;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.repository.RotaRepository;
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

        if (carga.getId() == null) {
            carga.setNumeroCargaExterno(dto.getNumCar().toString());
        } else {
            if (carga.getClientes() != null) carga.getClientes().clear();
            if (carga.getNotas() != null) carga.getNotas().clear();
        }

        var motoristaOpt = motoristaRepository
                .findByCodigoExterno(String.valueOf(dto.getCodMotorista()));

        if (motoristaOpt.isEmpty()) {
            log.warn("Motorista WinThor {} não encontrado na base FrotaPRO. Carga MDF-e {} ignorada.",
                    dto.getCodMotorista(), dto.getNumMdfe());
            return;
        }
        carga.setMotorista(motoristaOpt.get());

        var caminhaoOpt = caminhaoRepository
                .findByCodigoExterno(String.valueOf(dto.getCodVeiculo()));

        if (caminhaoOpt.isEmpty()) {
            log.warn("Caminhão WinThor {} não encontrado na base FrotaPRO. Carga MDF-e {} ignorada.",
                    dto.getCodVeiculo(), dto.getNumMdfe());
            return;
        }
        carga.setCaminhao(caminhaoOpt.get());

        String destino = dto.getDestino();

        var rota = rotaRepository.findByCidadeInicio(destino)
                .orElseGet(() -> {
                    Rota nova = new Rota();
                    nova.setCidadeInicio(destino);
                    return rotaRepository.save(nova);
                });

        carga.setRota(rota);

        if (dto.getPesoTotalKg() != null) {
            carga.setPesoCarga(BigDecimal.valueOf(dto.getPesoTotalKg()));
        } else {
            carga.setPesoCarga(null);
        }

        carga.setStatusCarga(Status.SINCRONIZADA);

        for (ClienteCargaWinThorDto cliDto : dto.getClientes()) {
            String label = cliDto.getCodCli() + " - " + cliDto.getNomeCli();

            CargaCliente cc = new CargaCliente();
            cc.setCarga(carga);
            cc.setCliente(label);

            carga.getClientes().add(cc);
        }

        Set<String> notasUnicas = dto.getClientes().stream()
                .flatMap(cli -> cli.getNotas().stream())
                .map(String::valueOf)
                .collect(Collectors.toCollection(HashSet::new));

        for (String numNota : notasUnicas) {
            CargaNota cn = new CargaNota();
            cn.setCarga(carga);
            cn.setNota(numNota);

            carga.getNotas().add(cn);
        }

        cargaRepository.save(carga);

        log.info("Carga externa={} sincronizada com {} clientes e {} notas",
                dto.getNumMdfe(), carga.getClientes().size(), carga.getNotas().size());
    }
}

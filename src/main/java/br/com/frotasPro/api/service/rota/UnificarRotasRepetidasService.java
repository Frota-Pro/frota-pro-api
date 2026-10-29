package br.com.frotasPro.api.service.rota;

import br.com.frotasPro.api.controller.response.UnificarRotasResponse;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.RotaRepository;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import br.com.frotasPro.api.util.NomeCidadeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Junta as rotas que o sync criou repetidas por causa de grafia ("POÇO DANTAS" x "POCO
 * DANTAS", "SOUSA" x "SOUSA FRIOS"), usando a mesma regra do sync (NomeCidadeUtils +
 * ParametroSistema.sufixosRotaIgnorados). Em cada grupo fica a rota de menor código:
 * as cargas das outras vão pra ela, as cidades e os dias são somados e as repetidas
 * são apagadas. Com simular = true só devolve o que seria feito.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnificarRotasRepetidasService {

    private final RotaRepository rotaRepository;
    private final CargaRepository cargaRepository;
    private final ParametroSistemaService parametroSistemaService;

    @Caching(evict = {
            @CacheEvict(value = "carga_listar", allEntries = true),
            @CacheEvict(value = "carga_buscar_numero", allEntries = true),
            @CacheEvict(value = "carga_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "carga_data_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_criacao", allEntries = true),
            @CacheEvict(value = "carga_motorista", allEntries = true),
            @CacheEvict(value = "carga_caminhao", allEntries = true),
            @CacheEvict(value = "carga_minha_atual", allEntries = true)
    })
    @Transactional
    public UnificarRotasResponse unificar(boolean simular) {
        List<String> sufixos = NomeCidadeUtils.sufixos(parametroSistemaService.buscarOuPadrao().getSufixosRotaIgnorados());

        Map<String, List<Rota>> porChave = new LinkedHashMap<>();
        rotaRepository.findAll().stream()
                .filter(r -> r.getCidadeInicio() != null)
                .sorted(Comparator.comparing(Rota::getCodigo))
                .forEach(r -> porChave.computeIfAbsent(NomeCidadeUtils.chave(r.getCidadeInicio(), sufixos),
                        k -> new ArrayList<>()).add(r));

        List<UnificarRotasResponse.Grupo> grupos = new ArrayList<>();
        int rotasRemovidas = 0;
        long cargasMovidas = 0;

        for (List<Rota> grupo : porChave.values()) {
            Rota mantida = grupo.get(0);
            String nomeAtual = mantida.getCidadeInicio();
            String nomeFinal = NomeCidadeUtils.nomeDaRota(nomeAtual, sufixos);
            List<Rota> repetidas = new ArrayList<>(grupo.subList(1, grupo.size()));

            if (repetidas.isEmpty() && nomeFinal.equals(nomeAtual)) {
                continue;
            }

            List<UnificarRotasResponse.Repetida> detalhe = new ArrayList<>();
            for (Rota repetida : repetidas) {
                long cargas = cargaRepository.countByRotaId(repetida.getId());
                cargasMovidas += cargas;
                detalhe.add(UnificarRotasResponse.Repetida.builder()
                        .codigo(repetida.getCodigo())
                        .nome(repetida.getCidadeInicio())
                        .quantidadeCargas(cargas)
                        .build());
            }
            grupos.add(UnificarRotasResponse.Grupo.builder()
                    .codigoMantida(mantida.getCodigo())
                    .nomeAtual(nomeAtual)
                    .nomeFinal(nomeFinal)
                    .repetidas(detalhe)
                    .build());
            rotasRemovidas += repetidas.size();

            if (simular) {
                continue;
            }

            for (Rota repetida : repetidas) {
                for (String cidade : repetida.getCidades()) {
                    if (!mantida.getCidades().contains(cidade)) {
                        mantida.getCidades().add(cidade);
                    }
                }
                mantida.setQuantidadeDeDias(Math.max(mantida.getQuantidadeDeDias(), repetida.getQuantidadeDeDias()));
            }
            mantida.setCidadeInicio(nomeFinal);
            rotaRepository.saveAndFlush(mantida);

            for (Rota repetida : repetidas) {
                cargaRepository.trocarRota(repetida.getId(), mantida.getId());
                rotaRepository.apagarSemCascata(repetida.getId());
                log.info("Rota {} ({}) unificada em {} ({})", repetida.getCodigo(), repetida.getCidadeInicio(),
                        mantida.getCodigo(), nomeFinal);
            }
        }

        return UnificarRotasResponse.builder()
                .simulacao(simular)
                .rotasRemovidas(rotasRemovidas)
                .cargasMovidas((int) cargasMovidas)
                .grupos(grupos)
                .build();
    }
}

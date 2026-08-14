package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Reconciliação: a sincronização normal só olha "o que existe hoje no
 * WinThor" e vai gravando — nunca verifica se uma carga que já foi
 * sincronizada antes sumiu de lá depois (pedido cancelado, minuta refeita,
 * etc). Sem isso, uma carga pode ficar parecendo válida no FrotaPRO
 * indefinidamente mesmo não existindo mais no WinThor.
 * <p>
 * Só verifica cargas SINCRONIZADA (ainda não iniciadas) — depois que o
 * motorista começa a rota, não faz mais sentido reavaliar isso aqui. Só
 * marca e notifica; nunca muda o status da carga sozinha.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificarCargasSumidasWinThorService {

    private final CargaRepository cargaRepository;
    private final CargaExistenciaWinThorClient cargaExistenciaWinThorClient;
    private final NotificacaoService notificacaoService;
    private final IntegracaoWinThorConfigService integracaoWinThorConfigService;

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
    public void verificar() {
        List<Carga> cargasSincronizadas = cargaRepository.findByStatusCargaAndNumeroCargaExternoIsNotNull(Status.SINCRONIZADA);

        if (cargasSincronizadas.isEmpty()) {
            return;
        }

        List<Integer> codigos = cargasSincronizadas.stream()
                .map(c -> parseNumeroCargaExterno(c.getNumeroCargaExterno()))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        if (codigos.isEmpty()) {
            return;
        }

        Set<Integer> existentes = cargaExistenciaWinThorClient.filtrarExistentes(codigos);
        var agora = FusoHorarioUtils.agoraBrasil();
        int novasSumidas = 0;

        for (Carga carga : cargasSincronizadas) {
            Integer numCar = parseNumeroCargaExterno(carga.getNumeroCargaExterno());
            if (numCar == null) {
                continue;
            }

            boolean existeNoWinThor = existentes.contains(numCar);
            carga.setDataVerificacaoWinThor(agora);

            if (!existeNoWinThor && !carga.isNaoEncontradaNoWinThor()) {
                carga.setNaoEncontradaNoWinThor(true);
                novasSumidas++;
                notificarCargaSumida(carga);
            } else if (existeNoWinThor && carga.isNaoEncontradaNoWinThor()) {
                // Reapareceu numa verificação seguinte — provavelmente foi um problema
                // transitório no WinThor, não uma exclusão de verdade.
                carga.setNaoEncontradaNoWinThor(false);
            }
        }

        cargaRepository.saveAll(cargasSincronizadas);

        if (novasSumidas > 0) {
            log.warn("Verificação de reconciliação: {} carga(s) sincronizada(s) não foram mais encontradas no WinThor.", novasSumidas);
        }
    }

    private void notificarCargaSumida(Carga carga) {
        String numeroCargaExibicao = CargaMapper.resolverNumeroExibicao(
                carga.getNumeroCarga(), carga.getNumeroCargaExterno(), integracaoWinThorConfigService.isCargaIntegracaoAtiva());

        notificacaoService.notificar(
                EventoNotificacao.CARGA_NAO_ENCONTRADA_WINTHOR,
                TipoNotificacao.ALERTA,
                "Carga não encontrada no WinThor",
                "A carga " + numeroCargaExibicao + " foi sincronizada antes, mas não foi mais encontrada no WinThor "
                        + "(sem nota fiscal vinculada ao carregamento " + carga.getNumeroCargaExterno() + "). "
                        + "Pode ter sido cancelada ou desvinculada lá — verifique antes de iniciar a viagem.",
                "CARGA",
                carga.getId(),
                numeroCargaExibicao
        );
    }

    private Integer parseNumeroCargaExterno(String numeroCargaExterno) {
        if (numeroCargaExterno == null || numeroCargaExterno.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(numeroCargaExterno.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

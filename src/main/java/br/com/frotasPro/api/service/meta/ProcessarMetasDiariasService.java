package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.util.FusoHorarioUtils;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.repository.NotificacaoRepository;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import br.com.frotasPro.api.util.MetaProgressoService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcessarMetasDiariasService {

    private final MetaRepository metaRepository;
    private final MetaCategoriaCaminhaoVinculoService metaCategoriaCaminhaoVinculoService;
    private final CaminhaoRepository caminhaoRepository;
    private final MetaProgressoService metaProgressoService;
    private final NotificacaoService notificacaoService;
    private final NotificacaoRepository notificacaoRepository;
    private final FechamentoMetaService fechamentoMetaService;

    // Job diário (1h da manhã) — não passa por controller, então nunca
    // invalidava esses caches: fecha/renova metas automaticamente todo dia,
    // mas a tela de Metas continuava mostrando o status de ontem (ou não
    // mostrava a meta recém-renovada) até alguém editar uma meta manualmente
    // pelo controller. Mesmo bug já corrigido antes pro sync de motorista/caminhão.
    @Caching(evict = {
            @CacheEvict(value = "meta_buscar_id", allEntries = true),
            @CacheEvict(value = "meta_listar", allEntries = true),
            @CacheEvict(value = "meta_ativas_caminhao", allEntries = true),
            @CacheEvict(value = "meta_historico", allEntries = true),
            @CacheEvict(value = "meta_historico_caminhao", allEntries = true)
    })
    @Transactional
    public void processar() {
        LocalDate hoje = FusoHorarioUtils.hojeBrasil();

        atualizarStatusAutomatico(hoje);
        notificarMetasVencendoForaDaMeta(hoje);

        List<Meta> metasVencidas = metaRepository
                .findByDataFimBeforeAndStatusMeta(hoje, StatusMeta.EM_ANDAMENTO);

        for (Meta meta : metasVencidas) {
            // Com o prazo de fechamento (dias úteis depois do fim do mês) a meta pode já ter
            // passado do dataFim e continuar em andamento — só conclui quando o mês fechou.
            if (fechamentoMetaService.metaFechada(meta, hoje)) {
                meta.setStatusMeta(StatusMeta.CONCLUIDA);
            }
        }

        // Renova toda meta que já passou do fim, também as ainda em andamento por causa do
        // prazo de fechamento: o mês seguinte precisa existir desde o dia 1, senão o motorista
        // ficaria sem meta nova até o prazo vencer. A checagem de conflito evita duplicar.
        List<Meta> metasConcluidas = metaRepository
                .findByDataFimBeforeAndStatusMetaIn(hoje, List.of(StatusMeta.EM_ANDAMENTO, StatusMeta.CONCLUIDA));

        for (Meta meta : metasConcluidas) {
            if (!meta.isRenovarAutomaticamente()) continue;

            LocalDate novoInicio = meta.getDataFim().plusDays(1);
            LocalDate novoFim = calcularDataFimRenovacao(meta, novoInicio);

            boolean jaExiste = metaRepository.existsMetaNaoCanceladaConflitante(
                    meta.getTipoMeta(),
                    novoInicio,
                    novoFim,
                    meta.getCaminhao(),
                    meta.getCategoria(),
                    meta.getMotorista(),
                    null
            );

            if (jaExiste) {
                continue;
            }

            Meta nova = new Meta();
            nova.setDataIncio(novoInicio);
            nova.setDataFim(novoFim);
            nova.setTipoMeta(meta.getTipoMeta());
            nova.setValorMeta(meta.getValorMeta());
            nova.setValorRealizado(BigDecimal.ZERO);
            nova.setUnidade(meta.getUnidade());
            nova.setStatusMeta(statusPorPeriodo(novoInicio, novoFim, hoje));
            nova.setDescricao(meta.getDescricao());
            nova.setCaminhao(meta.getCaminhao());
            nova.setCategoria(meta.getCategoria());
            nova.setMotorista(meta.getMotorista());
            nova.setRenovarAutomaticamente(meta.isRenovarAutomaticamente());

            Meta novaSalva = metaRepository.save(nova);
            metaCategoriaCaminhaoVinculoService.sincronizar(novaSalva);
        }
    }

    LocalDate calcularDataFimRenovacao(Meta meta, LocalDate novoInicio) {
        if (isMesCalendarioCompleto(meta)) {
            return novoInicio.withDayOfMonth(novoInicio.lengthOfMonth());
        }

        Period periodo = Period.between(meta.getDataIncio(), meta.getDataFim());
        return novoInicio.plus(periodo);
    }

    private boolean isMesCalendarioCompleto(Meta meta) {
        LocalDate inicio = meta.getDataIncio();
        LocalDate fim = meta.getDataFim();
        return inicio != null
                && fim != null
                && inicio.getDayOfMonth() == 1
                && fim.equals(inicio.withDayOfMonth(inicio.lengthOfMonth()));
    }

    private void atualizarStatusAutomatico(LocalDate hoje) {
        List<Meta> metas = metaRepository.findByStatusMetaNot(StatusMeta.CANCELADA);
        for (Meta meta : metas) {
            StatusMeta status = statusDaMeta(meta, hoje);
            if (status != null && meta.getStatusMeta() != status) {
                meta.setStatusMeta(status);
            }
        }
    }

    /** Igual a statusPorPeriodo, mas "concluída" só depois do prazo de fechamento da meta. */
    private StatusMeta statusDaMeta(Meta meta, LocalDate hoje) {
        if (meta.getDataIncio() == null || meta.getDataFim() == null) {
            return null;
        }
        if (hoje.isBefore(meta.getDataIncio())) {
            return StatusMeta.NAO_INICIADA;
        }
        return fechamentoMetaService.metaFechada(meta, hoje) ? StatusMeta.CONCLUIDA : StatusMeta.EM_ANDAMENTO;
    }

    private StatusMeta statusPorPeriodo(LocalDate inicio, LocalDate fim, LocalDate hoje) {
        if (inicio == null || fim == null) {
            return null;
        }
        if (hoje.isBefore(inicio)) {
            return StatusMeta.NAO_INICIADA;
        }
        if (hoje.isAfter(fim)) {
            return StatusMeta.CONCLUIDA;
        }
        return StatusMeta.EM_ANDAMENTO;
    }

    private void notificarMetasVencendoForaDaMeta(LocalDate hoje) {
        LocalDate fimJanela = hoje.plusDays(3);
        List<Meta> metas = metaRepository.findByStatusMetaInAndDataFimBetween(
                List.of(StatusMeta.EM_ANDAMENTO),
                hoje,
                fimJanela
        );

        for (Meta meta : metas) {
            if (meta.getCategoria() == null) {
                continue;
            }
            List<Caminhao> caminhoes = caminhaoRepository.findByCategoriaIdAndAtivoTrue(meta.getCategoria().getId());
            for (Caminhao caminhao : caminhoes) {
                BigDecimal realizado = metaProgressoService.calcularValorRealizado(meta, caminhao, null);
                boolean atingida = metaProgressoService.metaAtingida(meta.getTipoMeta(), realizado, meta.getValorMeta());
                if (atingida) {
                    continue;
                }
                String referenciaCodigo = meta.getId() + "|" + caminhao.getCodigo();
                boolean jaNotificouHoje = notificacaoRepository.existsByEventoAndReferenciaIdAndReferenciaCodigoAndCriadoEmAfter(
                        EventoNotificacao.META_CAMINHAO_FORA,
                        meta.getId(),
                        referenciaCodigo,
                        hoje.atStartOfDay()
                );
                if (jaNotificouHoje) {
                    continue;
                }
                notificacaoService.notificar(
                        EventoNotificacao.META_CAMINHAO_FORA,
                        TipoNotificacao.ALERTA,
                        "Caminhão fora da meta",
                        "Caminhão " + caminhao.getCodigo() + " está fora da meta " + meta.getTipoMeta()
                                + " da categoria " + meta.getCategoria().getCodigo()
                                + ". Meta: " + meta.getValorMeta()
                                + ", realizado: " + realizado + ".",
                        "META",
                        meta.getId(),
                        referenciaCodigo
                );
            }
        }
    }
}

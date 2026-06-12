package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.modules.notificacao.repository.NotificacaoRepository;
import br.com.frotasPro.api.modules.notificacao.service.NotificacaoService;
import br.com.frotasPro.api.util.MetaProgressoService;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public void processar() {
        LocalDate hoje = LocalDate.now();

        atualizarStatusAutomatico(hoje);
        notificarMetasVencendoForaDaMeta(hoje);

        List<Meta> metasVencidas = metaRepository
                .findByDataFimBeforeAndStatusMeta(hoje, StatusMeta.EM_ANDAMENTO);

        for (Meta meta : metasVencidas) {
            meta.setStatusMeta(StatusMeta.CONCLUIDA);
        }

        List<Meta> metasConcluidas = metaRepository
                .findByDataFimBeforeAndStatusMeta(hoje, StatusMeta.CONCLUIDA);

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
            StatusMeta status = statusPorPeriodo(meta.getDataIncio(), meta.getDataFim(), hoje);
            if (status != null && meta.getStatusMeta() != status) {
                meta.setStatusMeta(status);
            }
        }
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

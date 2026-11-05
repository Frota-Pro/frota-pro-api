package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import br.com.frotasPro.api.util.MetaProgressoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Fecha sozinho o mês da meta do motorista que já bateu TODAS as metas mensais do caminhão dele,
 * pra ele não precisar confirmar no app. Se alguma meta (tonelada, consumo, km...) ainda não foi
 * batida, o mês continua aberto pra ele finalizar. Quando ele finaliza uma carga e com isso bate
 * a última meta que faltava, o mês fecha na hora.
 *
 * <p>Só roda com o parâmetro {@code fecharMetaAutomaticamente} ligado e o prazo em dias úteis
 * ({@code diasUteisFechamentoMeta}) ativo. Metas conferidas: as de mês calendário completo do
 * caminhão titular do motorista (direto, ou pela categoria) e as cadastradas direto no motorista,
 * uma por tipo. Sem nenhuma meta cadastrada, não fecha (não dá pra dizer que bateu).
 */
@Slf4j
@Service
public class FechamentoAutomaticoMetaService {

    private static final List<StatusMeta> STATUS_VALIDOS =
            List.of(StatusMeta.NAO_INICIADA, StatusMeta.EM_ANDAMENTO, StatusMeta.CONCLUIDA);

    private final ParametroSistemaService parametroSistemaService;
    private final FechamentoMetaService fechamentoMetaService;
    private final MotoristaRepository motoristaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final MetaRepository metaRepository;
    private final MetaProgressoService metaProgressoService;
    private final PlatformTransactionManager transactionManager;

    public FechamentoAutomaticoMetaService(ParametroSistemaService parametroSistemaService,
                                           FechamentoMetaService fechamentoMetaService,
                                           MotoristaRepository motoristaRepository,
                                           CaminhaoRepository caminhaoRepository,
                                           MetaRepository metaRepository,
                                           MetaProgressoService metaProgressoService,
                                           PlatformTransactionManager transactionManager) {
        this.parametroSistemaService = parametroSistemaService;
        this.fechamentoMetaService = fechamentoMetaService;
        this.motoristaRepository = motoristaRepository;
        this.caminhaoRepository = caminhaoRepository;
        this.metaRepository = metaRepository;
        this.metaProgressoService = metaProgressoService;
        this.transactionManager = transactionManager;
    }

    public boolean isLigado() {
        return fechamentoMetaService.isAtivo() && parametroSistemaService.buscarOuPadrao().isFecharMetaAutomaticamente();
    }

    /** Job diário: confere todos os motoristas ativos com o mês anterior ainda aberto. Devolve quantos fechou. */
    public int processarTodos() {
        if (!isLigado()) {
            return 0;
        }
        LocalDate hoje = FusoHorarioUtils.hojeBrasil();
        YearMonth mes = YearMonth.from(hoje).minusMonths(1);
        if (hoje.isAfter(fechamentoMetaService.ultimoDiaAberto(mes))) {
            return 0;
        }

        int fechados = 0;
        for (Motorista motorista : motoristaRepository.findByAtivoTrueOrderByNomeAsc()) {
            if (fecharSePossivel(motorista.getId(), mes)) {
                fechados++;
            }
        }
        if (fechados > 0) {
            log.info("Fechamento automático da meta de {}: {} motorista(s) com todas as metas batidas.", mes, fechados);
        }
        return fechados;
    }

    /**
     * Chamado quando uma carga é finalizada: o motorista pode ter acabado de bater a última meta
     * do mês anterior (carga que saiu nos dias úteis a mais). Roda só DEPOIS do commit da
     * finalização, em transação própria: enxerga a carga já gravada e, se der qualquer erro, a
     * finalização do motorista não é afetada.
     */
    public void avaliarAposFinalizarCarga(Collection<Motorista> motoristas) {
        List<UUID> ids = new ArrayList<>(new LinkedHashSet<>(motoristas.stream()
                .filter(m -> m != null && m.getId() != null)
                .map(Motorista::getId)
                .toList()));
        if (ids.isEmpty()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    avaliarAgora(ids);
                }
            });
        } else {
            avaliarAgora(ids);
        }
    }

    private void avaliarAgora(List<UUID> motoristaIds) {
        try {
            if (!isLigado()) {
                return;
            }
            YearMonth mes = YearMonth.from(FusoHorarioUtils.hojeBrasil()).minusMonths(1);
            for (UUID id : motoristaIds) {
                fecharSePossivel(id, mes);
            }
        } catch (RuntimeException e) {
            log.warn("Fechamento automático da meta falhou (a finalização da carga não é afetada): {}", e.getMessage(), e);
        }
    }

    /** Uma transação por motorista: um erro num não derruba os outros. */
    private boolean fecharSePossivel(UUID motoristaId, YearMonth mes) {
        try {
            TransactionTemplate transacao = new TransactionTemplate(transactionManager);
            transacao.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            return Boolean.TRUE.equals(transacao.execute(status -> {
                Motorista motorista = motoristaRepository.findById(motoristaId).orElse(null);
                return motorista != null && fecharSePossivel(motorista, mes);
            }));
        } catch (RuntimeException e) {
            log.warn("Fechamento automático da meta do motorista {} falhou: {}", motoristaId, e.getMessage(), e);
            return false;
        }
    }

    boolean fecharSePossivel(Motorista motorista, YearMonth mes) {
        if (!fechamentoMetaService.podeFecharAutomaticamente(motorista, mes)) {
            return false;
        }
        Caminhao caminhao = caminhaoRepository.findByMotoristaTitularId(motorista.getId()).orElse(null);
        List<Meta> metas = metasMensais(caminhao, motorista, mes);
        if (metas.isEmpty()) {
            return false;
        }
        for (Meta meta : metas) {
            BigDecimal realizado = metaProgressoService.calcularValorRealizado(meta, caminhao, motorista);
            if (!Boolean.TRUE.equals(metaProgressoService.metaAtingida(meta.getTipoMeta(), realizado, meta.getValorMeta()))) {
                return false;
            }
        }
        return fechamentoMetaService.confirmarAutomaticamente(motorista, mes);
    }

    /** Uma meta por tipo: as do caminhão ou do motorista têm preferência sobre a da categoria. */
    private List<Meta> metasMensais(Caminhao caminhao, Motorista motorista, YearMonth mes) {
        LocalDate inicio = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();
        UUID caminhaoId = caminhao != null ? caminhao.getId() : null;

        Map<TipoMeta, Meta> porTipo = new EnumMap<>(TipoMeta.class);
        for (TipoMeta tipo : TipoMeta.values()) {
            metaRepository.buscarMetasAtivasConsumoPorCaminhaoOuMotorista(tipo, fim, caminhaoId, motorista.getId())
                    .stream()
                    .filter(m -> mesCompleto(m, inicio, fim))
                    .findFirst()
                    .ifPresent(m -> porTipo.put(tipo, m));
        }

        if (caminhao != null) {
            List<Meta> daCategoria = metaRepository.findMetasCategoriaVinculadasAoCaminhaoNaData(caminhaoId, STATUS_VALIDOS, fim);
            if (daCategoria.isEmpty() && caminhao.getCategoria() != null) {
                daCategoria = metaRepository.findByCategoriaIdAndStatusMetaInAndDataIncioLessThanEqualAndDataFimGreaterThanEqual(
                        caminhao.getCategoria().getId(), STATUS_VALIDOS, fim, fim);
            }
            daCategoria.stream()
                    .filter(m -> mesCompleto(m, inicio, fim))
                    .forEach(m -> porTipo.putIfAbsent(m.getTipoMeta(), m));
        }
        return new ArrayList<>(porTipo.values());
    }

    private boolean mesCompleto(Meta meta, LocalDate inicio, LocalDate fim) {
        return inicio.equals(meta.getDataIncio()) && fim.equals(meta.getDataFim());
    }
}

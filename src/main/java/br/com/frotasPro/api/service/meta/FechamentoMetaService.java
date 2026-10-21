package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.response.FechamentoMetaResponse;
import br.com.frotasPro.api.domain.FechamentoMetaMotorista;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.repository.FechamentoMetaMotoristaRepository;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import br.com.frotasPro.api.util.DiaUtilUtils;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Regra de "mês aberto/fechado" das metas.
 *
 * <p>Com {@code diasUteisFechamentoMeta = N > 0}, o mês M continua aberto até o N-ésimo dia
 * útil do mês seguinte: o motorista ganha esses dias pra bater a meta de M, e toda carga que
 * ele INICIAR nesse período conta em M. Dois jeitos de fechar: o prazo geral vencer, ou o
 * motorista confirmar no app que a meta dele está ok (cargas iniciadas depois disso contam no
 * mês seguinte). Ver {@link #calcularCompetencia}.
 *
 * <p>Com N = 0 (padrão) nada disso vale: tudo se comporta como antes (carga conta no mês
 * da data de saída, meta fecha no dia seguinte ao fim).
 */
@Service
@RequiredArgsConstructor
public class FechamentoMetaService {

    private final FechamentoMetaMotoristaRepository repository;
    private final ParametroSistemaService parametroSistemaService;

    public int diasUteis() {
        return Math.max(0, parametroSistemaService.buscarOuPadrao().getDiasUteisFechamentoMeta());
    }

    public boolean isAtivo() {
        return diasUteis() > 0;
    }

    /** Último dia em que o mês ainda está aberto pra todos. Desativado: o próprio último dia do mês. */
    public LocalDate ultimoDiaAberto(YearMonth mes) {
        return ultimoDiaAberto(mes, diasUteis());
    }

    LocalDate ultimoDiaAberto(YearMonth mes, int diasUteis) {
        LocalDate fimDoMes = mes.atEndOfMonth();
        return diasUteis > 0 ? DiaUtilUtils.enesimoDiaUtilApos(fimDoMes, diasUteis) : fimDoMes;
    }

    /**
     * Quando a meta deixa de ser "em andamento". Só metas de mês calendário completo ganham
     * a folga — meta de período livre (ex.: 10 a 25) continua fechando no dia seguinte ao fim.
     */
    public LocalDate ultimoDiaAbertoDaMeta(Meta meta) {
        if (meta.getDataFim() == null) {
            return null;
        }
        if (isAtivo() && isMesCalendarioCompleto(meta)) {
            return ultimoDiaAberto(YearMonth.from(meta.getDataFim()));
        }
        return meta.getDataFim();
    }

    public boolean metaFechada(Meta meta, LocalDate hoje) {
        LocalDate limite = ultimoDiaAbertoDaMeta(meta);
        if (limite == null) {
            return false;
        }
        if (hoje.isAfter(limite)) {
            return true;
        }
        // Motorista confirmou antes do prazo: a meta DELE fecha (só depois do mês acabar).
        return isAtivo()
                && meta.getMotorista() != null
                && isMesCalendarioCompleto(meta)
                && hoje.isAfter(meta.getDataFim())
                && confirmadoPeloMotorista(meta.getMotorista(), YearMonth.from(meta.getDataFim()));
    }

    public boolean confirmadoPeloMotorista(Motorista motorista, YearMonth mes) {
        return motorista != null && motorista.getId() != null
                && repository.existsByMotoristaIdAndMesReferenciaAndReabertoEmIsNull(motorista.getId(), mes.atDay(1));
    }

    /** O motorista já tinha confirmado a meta de {@code mes} no momento em que a carga saiu? */
    private boolean confirmadoAteASaida(Motorista motorista, YearMonth mes, LocalDate dtSaida, LocalDateTime dtHoraSaida) {
        if (motorista == null || motorista.getId() == null) {
            return false;
        }
        LocalDateTime saiuEm = dtHoraSaida != null ? dtHoraSaida : dtSaida.atStartOfDay();
        return repository.findByMotoristaIdAndMesReferencia(motorista.getId(), mes.atDay(1))
                .filter(FechamentoMetaMotorista::isAtivo)
                .map(f -> !f.getConfirmadoEm().isAfter(saiuEm))
                .orElse(false);
    }

    private boolean mesFechadoParaMotorista(Motorista motorista, YearMonth mes, LocalDate hoje) {
        return hoje.isAfter(ultimoDiaAberto(mes)) || confirmadoPeloMotorista(motorista, mes);
    }

    /**
     * Mês de competência de uma carga que está sendo finalizada agora. Devolve {@code null}
     * (= vale a data de saída, o normal) nos casos abaixo, em ordem:
     *
     * <ol>
     *   <li>A carga SAIU (foi iniciada) nos primeiros dias úteis de um mês, dentro do prazo de
     *       fechamento do mês anterior, e o motorista ainda não tinha confirmado a meta do mês
     *       anterior quando saiu: conta no mês anterior (devolve o dia 1 dele). É o que dá os
     *       "dias úteis a mais" pra bater a meta. Vale mesmo que ela só seja finalizada depois
     *       do prazo — o que conta é quando saiu, não quando voltou.</li>
     *   <li>Senão, se o mês da data de saída já estiver fechado pra esse motorista (prazo geral
     *       vencido ou confirmação), devolve o dia 1 do primeiro mês ainda aberto.</li>
     * </ol>
     */
    public LocalDate calcularCompetencia(LocalDate dtSaida, LocalDateTime dtHoraSaida, Motorista motorista, LocalDate hoje) {
        if (!isAtivo() || dtSaida == null) {
            return null;
        }
        YearMonth saida = YearMonth.from(dtSaida);
        YearMonth anterior = saida.minusMonths(1);
        if (!dtSaida.isAfter(ultimoDiaAberto(anterior))
                && !confirmadoAteASaida(motorista, anterior, dtSaida, dtHoraSaida)) {
            return anterior.atDay(1);
        }

        YearMonth base = saida;
        YearMonth alvo = base;
        YearMonth atual = YearMonth.from(hoje);

        while (alvo.isBefore(atual) && mesFechadoParaMotorista(motorista, alvo, hoje)) {
            alvo = alvo.plusMonths(1);
        }
        return alvo.equals(base) ? null : alvo.atDay(1);
    }

    @Transactional(readOnly = true)
    public FechamentoMetaResponse status(Motorista motorista, YearMonth mes) {
        return montarStatus(motorista, mes, FusoHorarioUtils.hojeBrasil());
    }

    /** O motorista (ou o admin) fecha o mês. Idempotente: confirmar de novo não muda nada. */
    @Transactional
    public FechamentoMetaResponse confirmar(Motorista motorista, YearMonth mes, String origem, String usuario) {
        if (!isAtivo()) {
            throw new BusinessException("O fechamento de meta com confirmação não está ativado no sistema.");
        }
        LocalDate hoje = FusoHorarioUtils.hojeBrasil();

        if (!hoje.isAfter(mes.atEndOfMonth())) {
            throw new BusinessException("O mês " + mes + " ainda não terminou — só dá pra confirmar a meta depois que o mês acabar.");
        }
        if (hoje.isAfter(ultimoDiaAberto(mes))) {
            throw new BusinessException("O prazo de fechamento do mês " + mes + " já venceu — o mês já está fechado.");
        }

        FechamentoMetaMotorista fechamento = repository
                .findByMotoristaIdAndMesReferencia(motorista.getId(), mes.atDay(1))
                .orElseGet(() -> {
                    FechamentoMetaMotorista novo = new FechamentoMetaMotorista();
                    novo.setMotorista(motorista);
                    novo.setMesReferencia(mes.atDay(1));
                    return novo;
                });

        if (fechamento.getId() == null || !fechamento.isAtivo()) {
            fechamento.setConfirmadoEm(FusoHorarioUtils.agoraBrasil());
            fechamento.setConfirmadoPor(usuario);
            fechamento.setOrigem(origem);
            fechamento.setReabertoEm(null);
            fechamento.setReabertoPor(null);
            repository.save(fechamento);
        }
        return montarStatus(motorista, mes, hoje);
    }

    /** Admin desfaz a confirmação. Cargas que já foram pro mês seguinte por causa dela NÃO voltam. */
    @Transactional
    public FechamentoMetaResponse reabrir(Motorista motorista, YearMonth mes, String usuario) {
        repository.findByMotoristaIdAndMesReferencia(motorista.getId(), mes.atDay(1))
                .filter(FechamentoMetaMotorista::isAtivo)
                .ifPresent(f -> {
                    f.setReabertoEm(FusoHorarioUtils.agoraBrasil());
                    f.setReabertoPor(usuario);
                    repository.save(f);
                });
        return montarStatus(motorista, mes, FusoHorarioUtils.hojeBrasil());
    }

    private FechamentoMetaResponse montarStatus(Motorista motorista, YearMonth mes, LocalDate hoje) {
        int dias = diasUteis();
        LocalDate limite = ultimoDiaAberto(mes, dias);
        var confirmacao = repository.findByMotoristaIdAndMesReferencia(motorista.getId(), mes.atDay(1))
                .filter(FechamentoMetaMotorista::isAtivo);

        String situacao;
        boolean podeConfirmar = false;
        if (dias <= 0) {
            situacao = "DESATIVADO";
        } else if (!hoje.isAfter(mes.atEndOfMonth())) {
            situacao = "EM_ANDAMENTO";
        } else if (confirmacao.isPresent()) {
            situacao = "CONFIRMADO";
        } else if (hoje.isAfter(limite)) {
            situacao = "FECHADO";
        } else {
            situacao = "ABERTO";
            podeConfirmar = true;
        }

        return FechamentoMetaResponse.builder()
                .mesReferencia(mes.atDay(1))
                .ativo(dias > 0)
                .diasUteisFechamento(dias)
                .dataLimite(limite)
                .situacao(situacao)
                .podeConfirmar(podeConfirmar)
                .confirmadoPeloMotorista(confirmacao.isPresent())
                .confirmadoEm(confirmacao.map(FechamentoMetaMotorista::getConfirmadoEm).orElse(null))
                .origem(confirmacao.map(FechamentoMetaMotorista::getOrigem).orElse(null))
                .build();
    }

    private boolean isMesCalendarioCompleto(Meta meta) {
        LocalDate inicio = meta.getDataIncio();
        LocalDate fim = meta.getDataFim();
        return inicio != null
                && fim != null
                && inicio.getDayOfMonth() == 1
                && fim.equals(inicio.withDayOfMonth(inicio.lengthOfMonth()));
    }
}

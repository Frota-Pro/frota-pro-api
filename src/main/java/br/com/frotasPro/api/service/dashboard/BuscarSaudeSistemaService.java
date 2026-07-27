package br.com.frotasPro.api.service.dashboard;

import br.com.frotasPro.api.controller.response.MotoristaAcessoResponse;
import br.com.frotasPro.api.controller.response.MotoristaAtrasoResponse;
import br.com.frotasPro.api.controller.response.SaudeSistemaResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BuscarSaudeSistemaService {

    private static final int JANELA_ATIVO_7_DIAS = 7;
    private static final int JANELA_ATIVO_30_DIAS = 30;
    private static final int TAMANHO_RANKING_ATRASO = 10;

    private final MotoristaRepository motoristaRepository;
    private final CargaRepository cargaRepository;

    @Transactional(readOnly = true)
    public SaudeSistemaResponse buscar(LocalDate inicio, LocalDate fim) {
        LocalDate periodoInicio = inicio != null ? inicio : LocalDate.now().withDayOfMonth(1);
        LocalDate periodoFim = fim != null
                ? fim
                : periodoInicio.withDayOfMonth(periodoInicio.lengthOfMonth());

        SaudeSistemaResponse.SaudeSistemaResponseBuilder builder = SaudeSistemaResponse.builder()
                .periodoInicio(periodoInicio)
                .periodoFim(periodoFim);

        preencherAdocaoApp(builder);
        preencherAtrasos(builder, periodoInicio, periodoFim);

        return builder.build();
    }

    private void preencherAdocaoApp(SaudeSistemaResponse.SaudeSistemaResponseBuilder builder) {
        List<Motorista> motoristas = motoristaRepository.listarAtivosComUsuarioVinculado();
        LocalDateTime agora = LocalDateTime.now();

        List<MotoristaAcessoResponse> linhas = new ArrayList<>();
        int ativos7 = 0;
        int ativos30 = 0;
        int nuncaAcessaram = 0;
        long totalAcessos = 0;

        for (Motorista motorista : motoristas) {
            Usuario usuario = motorista.getUsuario();
            LocalDateTime ultimoLogin = usuario.getUltimoLoginEm();
            long totalLogins = usuario.getTotalLogins();
            totalAcessos += totalLogins;

            Long diasSemAcesso = ultimoLogin != null
                    ? ChronoUnit.DAYS.between(ultimoLogin.toLocalDate(), agora.toLocalDate())
                    : null;

            if (diasSemAcesso == null) {
                nuncaAcessaram++;
            } else {
                if (diasSemAcesso <= JANELA_ATIVO_7_DIAS) ativos7++;
                if (diasSemAcesso <= JANELA_ATIVO_30_DIAS) ativos30++;
            }

            linhas.add(MotoristaAcessoResponse.builder()
                    .codigoMotorista(motorista.getCodigo())
                    .nomeMotorista(motorista.getNome())
                    .ultimoLoginEm(ultimoLogin)
                    .totalLogins(totalLogins)
                    .diasSemAcesso(diasSemAcesso)
                    .dispositivoAppVersao(usuario.getDispositivoAppVersao())
                    .dispositivoAppReportadoEm(usuario.getDispositivoAppReportadoEm())
                    .build());
        }

        // Quem está há mais tempo sem acessar (ou nunca acessou) aparece primeiro.
        linhas.sort(Comparator.comparing(
                MotoristaAcessoResponse::getUltimoLoginEm,
                Comparator.nullsFirst(Comparator.naturalOrder())
        ));

        builder.totalMotoristasComUsuario(motoristas.size())
                .motoristasAtivosUltimos7Dias(ativos7)
                .motoristasAtivosUltimos30Dias(ativos30)
                .motoristasNuncaAcessaram(nuncaAcessaram)
                .totalAcessosAcumulado(totalAcessos)
                .motoristas(linhas);
    }

    private void preencherAtrasos(SaudeSistemaResponse.SaudeSistemaResponseBuilder builder,
                                   LocalDate periodoInicio,
                                   LocalDate periodoFim) {
        List<Carga> cargasFinalizadas = cargaRepository.buscarFinalizadasComMotoristaNoPeriodo(periodoInicio, periodoFim);

        long total = cargasFinalizadas.size();
        long comAtrasoInicio = 0;
        long comAtrasoFim = 0;
        long somaAtrasoInicio = 0;
        long somaAtrasoFim = 0;

        // codigoMotorista -> [totalCargas, atrasoInicioCount, atrasoFimCount, somaAtrasoInicio, somaAtrasoFim]
        Map<String, long[]> statsPorMotorista = new LinkedHashMap<>();
        Map<String, String> nomePorMotorista = new LinkedHashMap<>();

        for (Carga carga : cargasFinalizadas) {
            long atrasoInicio = carga.calcularAtrasoInicio();
            long atrasoFim = carga.calcularAtraso();

            if (atrasoInicio > 0) {
                comAtrasoInicio++;
                somaAtrasoInicio += atrasoInicio;
            }
            if (atrasoFim > 0) {
                comAtrasoFim++;
                somaAtrasoFim += atrasoFim;
            }

            Motorista motorista = carga.getMotorista();
            String codigo = motorista != null ? motorista.getCodigo() : "—";
            String nome = motorista != null ? motorista.getNome() : "Sem motorista";
            nomePorMotorista.putIfAbsent(codigo, nome);

            long[] stats = statsPorMotorista.computeIfAbsent(codigo, k -> new long[5]);
            stats[0]++;
            if (atrasoInicio > 0) stats[1]++;
            if (atrasoFim > 0) stats[2]++;
            stats[3] += atrasoInicio;
            stats[4] += atrasoFim;
        }

        List<MotoristaAtrasoResponse> ranking = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : statsPorMotorista.entrySet()) {
            long[] stats = entry.getValue();
            ranking.add(MotoristaAtrasoResponse.builder()
                    .codigoMotorista(entry.getKey())
                    .nomeMotorista(nomePorMotorista.get(entry.getKey()))
                    .totalCargas(stats[0])
                    .cargasAtrasoInicio(stats[1])
                    .cargasAtrasoFim(stats[2])
                    .mediaAtrasoInicioDias(media(stats[3], stats[0]))
                    .mediaAtrasoFimDias(media(stats[4], stats[0]))
                    .build());
        }
        ranking.sort(Comparator.comparing(MotoristaAtrasoResponse::getMediaAtrasoFimDias).reversed());
        List<MotoristaAtrasoResponse> topRanking = ranking.stream()
                .limit(TAMANHO_RANKING_ATRASO)
                .toList();

        builder.totalCargasFinalizadasPeriodo(total)
                .cargasComAtrasoInicio(comAtrasoInicio)
                .cargasComAtrasoFim(comAtrasoFim)
                .percentualAtrasoInicio(percentual(comAtrasoInicio, total))
                .percentualAtrasoFim(percentual(comAtrasoFim, total))
                .atrasoMedioInicioDias(media(somaAtrasoInicio, total))
                .atrasoMedioFimDias(media(somaAtrasoFim, total))
                .rankingAtrasoMotoristas(topRanking);
    }

    private BigDecimal media(long soma, long total) {
        if (total == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(soma).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentual(long parte, long total) {
        if (total == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(parte)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
}

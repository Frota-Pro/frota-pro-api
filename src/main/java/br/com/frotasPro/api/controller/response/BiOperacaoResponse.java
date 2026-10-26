package br.com.frotasPro.api.controller.response;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Painel "Operação" do gestor, já calculado no servidor com as mesmas regras do dashboard do
 * Metabase (km válido de 1 a 3.000, abastecimento com preço anômalo fora da soma etc.).
 * Os meses vão no formato {@code yyyy-MM}.
 */
public record BiOperacaoResponse(
        int meses,
        OffsetDateTime geradoEm,
        Kpis kpis,
        List<MesCargas> cargasPorMes,
        List<MesCombustivel> combustivelPorMes,
        List<RotaCargas> cargasPorRota,
        List<ConsumoCategoria> consumoPorCategoria,
        List<MesMetas> metasPorMes
) {

    public record Kpis(
            long cargas,
            long entregas,
            long notasFiscais,
            long kmRodados,
            Double litros,
            Double gastoCombustivel,
            Double precoMedioLitro,
            Double custoPorKm
    ) {
    }

    public record MesCargas(String mes, long cargas, long entregas, long notas) {
    }

    public record MesCombustivel(String mes, Double litros, Double gasto, Double precoMedio) {
    }

    public record RotaCargas(String rota, long cargas) {
    }

    public record ConsumoCategoria(String categoria, Double kmPorLitro, Double metaKmPorLitro, Double percentualMeta, long viagens) {
    }

    public record MesMetas(String mes, Double consumoPct, Double toneladasPct, long semDados) {
    }
}

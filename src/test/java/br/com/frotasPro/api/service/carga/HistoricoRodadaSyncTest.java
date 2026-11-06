package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.integracao.CargaSyncItem;
import br.com.frotasPro.api.integracao.dto.CargaWinThorDto;
import br.com.frotasPro.api.service.carga.HistoricoRodadaSync.FotoCarga;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoricoRodadaSyncTest {

    private static final UUID JOB = UUID.randomUUID();
    private static final UUID CARGA_ID = UUID.randomUUID();

    private static FotoCarga foto(String peso, String valor, String status, boolean bloqueada) {
        return new FotoCarga(new BigDecimal(peso), new BigDecimal(valor), status, "Valzimar", "RLU2E87",
                LocalDate.of(2026, 11, 3), 52, bloqueada);
    }

    private static CargaWinThorDto dto(double pesoTon, String valor, Boolean transferencia, List<String> devolucoes) {
        CargaWinThorDto dto = new CargaWinThorDto();
        dto.setNumCar(422238);
        dto.setPesoTotalKg(pesoTon);
        dto.setValorTotal(new BigDecimal(valor));
        dto.setTemTransferencia(transferencia);
        dto.setCodigosDevolucao(devolucoes);
        return dto;
    }

    @Test
    void cargaNovaViraItemNova() {
        HistoricoRodadaSync rodada = new HistoricoRodadaSync(JOB);

        rodada.registrarProcessada("422788", CARGA_ID, null, foto("1.200", "9000.00", "SINCRONIZADA", false),
                dto(1.2, "9000.00", false, List.of()));

        assertEquals(1, rodada.getNovas());
        assertEquals(1, rodada.getItens().size());
        assertEquals(CargaSyncItem.NOVA, rodada.getItens().get(0).getTipo());
    }

    @Test
    void cargaSemMudancaSoContaENaoGeraItem() {
        HistoricoRodadaSync rodada = new HistoricoRodadaSync(JOB);

        // 6.890 e 6.89 são o mesmo número: a escala não pode virar "mudança"
        rodada.registrarProcessada("422238", CARGA_ID, foto("6.890", "66108.54", "FINALIZADA", false),
                foto("6.89", "66108.540", "FINALIZADA", false), dto(6.89, "66108.54", false, List.of()));

        assertEquals(1, rodada.getSemMudanca());
        assertEquals(0, rodada.getAtualizadas());
        assertTrue(rodada.getItens().isEmpty());
    }

    @Test
    void diminuicaoPorTransferenciaGuardaAntesDepoisEMotivo() {
        HistoricoRodadaSync rodada = new HistoricoRodadaSync(JOB);

        rodada.registrarProcessada("422238", CARGA_ID, foto("6.890", "66108.54", "FINALIZADA", false),
                foto("4.394", "42632.96", "FINALIZADA", false), dto(4.394, "42632.96", true, List.of()));

        assertEquals(1, rodada.getAtualizadas());
        CargaSyncItem item = rodada.getItens().get(0);
        assertEquals(CargaSyncItem.ATUALIZADA, item.getTipo());
        assertEquals(0, new BigDecimal("66108.54").compareTo(item.getValorAntes()));
        assertEquals(0, new BigDecimal("42632.96").compareTo(item.getValorDepois()));
        assertEquals(0, new BigDecimal("6.890").compareTo(item.getPesoAntes()));
        assertEquals(0, new BigDecimal("4.394").compareTo(item.getPesoDepois()));
        assertTrue(item.getMotivo().contains("transferência"), item.getMotivo());
    }

    @Test
    void diminuicaoPorDevolucaoCitaOCodigo() {
        HistoricoRodadaSync rodada = new HistoricoRodadaSync(JOB);

        rodada.registrarProcessada("422238", CARGA_ID, foto("6.890", "66108.54", "FINALIZADA", false),
                foto("6.000", "60000.00", "FINALIZADA", false), dto(6.0, "60000.00", false, List.of("53")));

        assertTrue(rodada.getItens().get(0).getMotivo().contains("devolução"));
        assertTrue(rodada.getItens().get(0).getMotivo().contains("53"));
    }

    @Test
    void diminuicaoRetidaGuardaOQueOWinthorDisseEOQueFicou() {
        HistoricoRodadaSync rodada = new HistoricoRodadaSync(JOB);

        // o sync não aplicou a diminuição: antes e depois iguais, mas marcada como retida agora
        rodada.registrarProcessada("422238", CARGA_ID, foto("6.890", "66108.54", "FINALIZADA", false),
                foto("6.890", "66108.54", "FINALIZADA", true), dto(4.394, "42632.96", false, List.of()));

        assertEquals(1, rodada.getDiminuicaoBloqueada());
        assertEquals(0, rodada.getAtualizadas());
        CargaSyncItem item = rodada.getItens().get(0);
        assertEquals(CargaSyncItem.BLOQUEADA, item.getTipo());
        assertEquals(0, new BigDecimal("42632.96").compareTo(item.getValorWinthor()));
        assertEquals(0, new BigDecimal("4.394").compareTo(item.getPesoWinthor()));
        assertEquals(0, new BigDecimal("66108.54").compareTo(item.getValorDepois()));
    }

    @Test
    void retidaQueJaEstavaRetidaESemMudancaNaoRepeteOItem() {
        HistoricoRodadaSync rodada = new HistoricoRodadaSync(JOB);

        rodada.registrarProcessada("422238", CARGA_ID, foto("6.890", "66108.54", "FINALIZADA", true),
                foto("6.890", "66108.54", "FINALIZADA", true), dto(4.394, "42632.96", false, List.of()));

        assertEquals(1, rodada.getDiminuicaoBloqueada(), "continua sendo contada no resumo da rodada");
        assertTrue(rodada.getItens().isEmpty(), "mas não vira uma linha nova a cada rodada");
    }

    @Test
    void mudancaDeMotoristaEStatusApareceNoMotivo() {
        HistoricoRodadaSync rodada = new HistoricoRodadaSync(JOB);
        FotoCarga antes = foto("6.890", "66108.54", "SINCRONIZADA", false);
        FotoCarga depois = new FotoCarga(antes.peso(), antes.valor(), "EM_ROTA", "Outro Motorista", antes.caminhao(),
                antes.dtFaturamento(), antes.notas(), false);

        rodada.registrarProcessada("422238", CARGA_ID, antes, depois, dto(6.89, "66108.54", false, List.of()));

        String motivo = rodada.getItens().get(0).getMotivo();
        assertTrue(motivo.contains("Status SINCRONIZADA → EM_ROTA"), motivo);
        assertTrue(motivo.contains("Motorista Valzimar → Outro Motorista"), motivo);
    }

    @Test
    void descartadaGuardaOQueOWinthorDizia() {
        HistoricoRodadaSync rodada = new HistoricoRodadaSync(JOB);
        CargaWinThorDto dto = dto(1.5, "4703.85", false, List.of());
        dto.setNumCar(420562);

        rodada.registrarDescartada(dto, "Motorista não cadastrado no FrotaPRO (código do motorista no WinThor: 493)");

        assertEquals(1, rodada.getDescartadas());
        CargaSyncItem item = rodada.getItens().get(0);
        assertEquals(CargaSyncItem.DESCARTADA, item.getTipo());
        assertEquals("420562", item.getNumeroCargaExterno());
        assertEquals(0, new BigDecimal("4703.85").compareTo(item.getValorWinthor()));
        assertTrue(item.getMotivo().contains("493"));
    }
}

package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.integracao.CargaSyncItem;
import br.com.frotasPro.api.integracao.dto.CargaWinThorDto;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Junta, durante uma rodada de sincronização, o que aconteceu em cada carga (criada, atualizada, diminuição retida,
 * descartada ou sem mudança) para gravar depois, de uma vez, no histórico. Só compara e descreve: não altera nada.
 * Peso em TONELADAS, valor em R$.
 */
public class HistoricoRodadaSync {

    /** "Foto" dos campos que o histórico acompanha, tirada antes e depois de a carga ser sincronizada. */
    public record FotoCarga(
            BigDecimal peso,
            BigDecimal valor,
            String status,
            String motorista,
            String caminhao,
            LocalDate dtFaturamento,
            Integer notas,
            boolean diminuicaoBloqueada
    ) {
    }

    private final UUID jobId;
    private final List<CargaSyncItem> itens = new ArrayList<>();
    private int novas;
    private int atualizadas;
    private int semMudanca;
    private int descartadas;
    private int diminuicaoBloqueada;

    public HistoricoRodadaSync(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public List<CargaSyncItem> getItens() {
        return itens;
    }

    public int getNovas() {
        return novas;
    }

    public int getAtualizadas() {
        return atualizadas;
    }

    public int getSemMudanca() {
        return semMudanca;
    }

    public int getDescartadas() {
        return descartadas;
    }

    public int getDiminuicaoBloqueada() {
        return diminuicaoBloqueada;
    }

    public void registrarDescartada(CargaWinThorDto dto, String motivo) {
        descartadas++;
        CargaSyncItem item = novoItem(String.valueOf(dto.getNumCar()), null, CargaSyncItem.DESCARTADA);
        item.setMotivo(limitar(motivo + " (WinThor: " + descreverValores(valor(dto.getValorTotal()), peso(dto)) + ")"));
        item.setPesoWinthor(peso(dto));
        item.setValorWinthor(valor(dto.getValorTotal()));
        itens.add(item);
    }

    /**
     * @param antes foto antes de sincronizar, ou null se a carga é nova
     */
    public void registrarProcessada(String numeroCarga, UUID cargaId, FotoCarga antes, FotoCarga depois, CargaWinThorDto dto) {
        boolean nova = antes == null;
        boolean mudou = nova || mudou(antes, depois);
        boolean retida = depois.diminuicaoBloqueada();
        boolean ficouRetida = retida && (antes == null || !antes.diminuicaoBloqueada());

        if (retida) {
            diminuicaoBloqueada++;
        }
        if (nova) {
            novas++;
        } else if (mudou) {
            atualizadas++;
        } else {
            semMudanca++;
        }

        String tipo;
        if (nova) {
            tipo = CargaSyncItem.NOVA;
        } else if (retida && (ficouRetida || mudou)) {
            tipo = CargaSyncItem.BLOQUEADA;
        } else if (mudou) {
            tipo = CargaSyncItem.ATUALIZADA;
        } else {
            return; // sem mudança: não enche o histórico
        }

        CargaSyncItem item = novoItem(numeroCarga, cargaId, tipo);
        if (antes != null) {
            item.setPesoAntes(antes.peso());
            item.setValorAntes(antes.valor());
            item.setStatusAntes(antes.status());
            item.setMotoristaAntes(antes.motorista());
            item.setCaminhaoAntes(antes.caminhao());
            item.setDtFaturamentoAntes(antes.dtFaturamento());
            item.setNotasAntes(antes.notas());
        }
        item.setPesoDepois(depois.peso());
        item.setValorDepois(depois.valor());
        item.setStatusDepois(depois.status());
        item.setMotoristaDepois(depois.motorista());
        item.setCaminhaoDepois(depois.caminhao());
        item.setDtFaturamentoDepois(depois.dtFaturamento());
        item.setNotasDepois(depois.notas());
        item.setPesoWinthor(peso(dto));
        item.setValorWinthor(valor(dto.getValorTotal()));
        item.setMotivo(limitar(CargaSyncItem.BLOQUEADA.equals(tipo)
                ? motivoRetida(depois, dto)
                : motivo(antes, depois, dto)));
        itens.add(item);
    }

    // ---------------------------------------------------------------------------------------------------------

    static boolean mudou(FotoCarga a, FotoCarga d) {
        return !mesmoNumero(a.peso(), d.peso())
                || !mesmoNumero(a.valor(), d.valor())
                || !Objects.equals(a.status(), d.status())
                || !Objects.equals(a.motorista(), d.motorista())
                || !Objects.equals(a.caminhao(), d.caminhao())
                || !Objects.equals(a.dtFaturamento(), d.dtFaturamento())
                || !Objects.equals(a.notas(), d.notas());
    }

    private static boolean mesmoNumero(BigDecimal x, BigDecimal y) {
        if (x == null || y == null) {
            return x == y;
        }
        return x.compareTo(y) == 0;
    }

    private static String motivo(FotoCarga antes, FotoCarga depois, CargaWinThorDto dto) {
        if (antes == null) {
            return "Carga criada a partir do WinThor";
        }
        List<String> partes = new ArrayList<>();

        boolean diminuiu = menor(depois.peso(), antes.peso()) || menor(depois.valor(), antes.valor());
        boolean aumentou = maior(depois.peso(), antes.peso()) || maior(depois.valor(), antes.valor());
        if (diminuiu) {
            if (Boolean.TRUE.equals(dto.getTemTransferencia())) {
                partes.add("Diminuiu (carga com transferência de pedido no WinThor)");
            } else if (dto.getCodigosDevolucao() != null && !dto.getCodigosDevolucao().isEmpty()) {
                partes.add("Diminuiu (devolução no WinThor, código " + String.join(", ", dto.getCodigosDevolucao()) + ")");
            } else {
                partes.add("Diminuiu (a minuta do WinThor ficou menor)");
            }
        } else if (aumentou) {
            partes.add("Aumentou (a minuta do WinThor ficou maior, ex.: pedido recebido de outra carga)");
        }
        if (!Objects.equals(antes.status(), depois.status())) {
            partes.add("Status " + antes.status() + " → " + depois.status());
        }
        if (!Objects.equals(antes.motorista(), depois.motorista())) {
            partes.add("Motorista " + antes.motorista() + " → " + depois.motorista());
        }
        if (!Objects.equals(antes.caminhao(), depois.caminhao())) {
            partes.add("Caminhão " + antes.caminhao() + " → " + depois.caminhao());
        }
        if (!Objects.equals(antes.dtFaturamento(), depois.dtFaturamento())) {
            partes.add("Data de faturamento " + antes.dtFaturamento() + " → " + depois.dtFaturamento());
        }
        if (!Objects.equals(antes.notas(), depois.notas())) {
            partes.add("Notas " + antes.notas() + " → " + depois.notas());
        }
        return partes.isEmpty() ? "Atualizada pelo WinThor" : String.join("; ", partes);
    }

    private static String motivoRetida(FotoCarga depois, CargaWinThorDto dto) {
        return "Diminuição retida pelo bloqueio: o WinThor diz "
                + descreverValores(valor(dto.getValorTotal()), peso(dto))
                + ", mas não há transferência nem devolução reconhecida; o FrotaPRO manteve "
                + descreverValores(depois.valor(), depois.peso());
    }

    private static String descreverValores(BigDecimal valor, BigDecimal pesoTon) {
        NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
        NumberFormat ton = NumberFormat.getNumberInstance(Locale.of("pt", "BR"));
        ton.setMinimumFractionDigits(3);
        ton.setMaximumFractionDigits(3);
        String v = valor == null ? "valor n/d" : moeda.format(valor);
        String p = pesoTon == null ? "peso n/d" : ton.format(pesoTon) + " t";
        return v + " e " + p;
    }

    private static boolean menor(BigDecimal novo, BigDecimal velho) {
        return novo != null && velho != null && novo.compareTo(velho) < 0;
    }

    private static boolean maior(BigDecimal novo, BigDecimal velho) {
        return novo != null && velho != null && novo.compareTo(velho) > 0;
    }

    private static BigDecimal peso(CargaWinThorDto dto) {
        return dto.getPesoTotalKg() != null ? BigDecimal.valueOf(dto.getPesoTotalKg()) : null; // o campo vem em toneladas
    }

    private static BigDecimal valor(BigDecimal valor) {
        return valor;
    }

    private CargaSyncItem novoItem(String numeroCarga, UUID cargaId, String tipo) {
        CargaSyncItem item = new CargaSyncItem();
        item.setJobId(jobId);
        item.setNumeroCargaExterno(numeroCarga);
        item.setCargaId(cargaId);
        item.setTipo(tipo);
        item.setCriadoEm(LocalDateTime.now());
        return item;
    }

    private static String limitar(String texto) {
        if (texto == null || texto.length() <= 500) {
            return texto;
        }
        return texto.substring(0, 500);
    }
}

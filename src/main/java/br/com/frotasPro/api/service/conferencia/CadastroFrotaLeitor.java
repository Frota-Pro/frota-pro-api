package br.com.frotasPro.api.service.conferencia;

import br.com.frotasPro.api.domain.enums.MotivoCargaSyncDescartada;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.Frota;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Lê, só leitura, o cadastro da FROTA (códigos do WinThor dos caminhões e motoristas) e as cargas que o sync
 * descartou, para a conferência saber o que é da frota e o que é de terceiro.
 */
@Component
@RequiredArgsConstructor
public class CadastroFrotaLeitor {

    private final JdbcTemplate jdbc;

    /** Motorista "externo" (criado pelo sync para TV10 de terceiro) não conta como da frota. */
    public Frota frota() {
        Set<String> caminhoes = new HashSet<>(jdbc.queryForList(
                "SELECT trim(codigo_externo) FROM tb_caminhao WHERE codigo_externo IS NOT NULL", String.class));
        Set<String> motoristas = new HashSet<>(jdbc.queryForList(
                "SELECT trim(codigo_externo) FROM tb_motorista WHERE codigo_externo IS NOT NULL AND externo = false", String.class));
        return new Frota(caminhoes, motoristas);
    }

    /** numCar -> motivo (texto) pelo qual o sync descartou a carga. */
    public Map<Integer, String> descartadas() {
        Map<Integer, String> r = new HashMap<>();
        jdbc.query("SELECT numero_carga_externo, motivo FROM tb_carga_sync_descartada", rs -> {
            try {
                Integer numCar = Integer.valueOf(rs.getString("numero_carga_externo").trim());
                String motivo = rs.getString("motivo");
                String texto;
                try {
                    texto = MotivoCargaSyncDescartada.valueOf(motivo).getDescricao();
                } catch (IllegalArgumentException e) {
                    texto = motivo;
                }
                r.put(numCar, texto);
            } catch (NumberFormatException ignorada) {
                // número de carga que não é inteiro: não entra na conferência
            }
        });
        return r;
    }
}

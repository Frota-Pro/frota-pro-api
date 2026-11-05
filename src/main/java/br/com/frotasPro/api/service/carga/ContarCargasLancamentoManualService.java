package br.com.frotasPro.api.service.carga;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Quantas cargas recentes precisam de lançamento manual de km e abastecimento: as de motorista de terceiro
 * (criado pelo sync, sem acesso ao app) e as de manifesto sem nota, que ainda estão sem km inicial ou final.
 * Alimenta o contador do menu "Cargas", para o administrador ver sem precisar procurar.
 */
@Service
@RequiredArgsConstructor
public class ContarCargasLancamentoManualService {

    /** Só olha o que é recente: carga antiga sem km é caso encerrado, não pendência. */
    static final int DIAS = 90;

    private final JdbcTemplate jdbc;

    public long contar() {
        Long total = jdbc.queryForObject("""
                SELECT count(*)
                  FROM tb_carga c
                  JOIN tb_motorista m ON m.id = c.motorista_id
                 WHERE (m.externo OR c.origem_carga = 'MANIFESTO_SEM_NOTA')
                   AND (c.km_inicial IS NULL OR c.km_final IS NULL)
                   AND c.data_faturamento >= current_date - ?
                """, Long.class, DIAS);
        return total == null ? 0 : total;
    }
}

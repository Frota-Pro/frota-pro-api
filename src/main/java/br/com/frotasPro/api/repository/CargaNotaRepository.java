package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.CargaNota;
import br.com.frotasPro.api.domain.CargaNotaId;
import br.com.frotasPro.api.projections.CidadeResumoProjection;
import br.com.frotasPro.api.projections.ClienteHistoricoRotaProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface CargaNotaRepository extends JpaRepository<CargaNota, CargaNotaId> {

    @Modifying
    @Transactional
    @Query("DELETE FROM CargaNota cn WHERE cn.carga.id = :cargaId")
    void deleteByCargaId(UUID cargaId);

    // Cidade não é chave de agrupamento (cliente é) — na prática é constante
    // por cliente, então max(cn.cidade) só serve pra pegar o valor sem
    // precisar incluir a coluna no GROUP BY.
    // Endereço vem de cn.clienteRef (tabela Cliente) — igual cidade, na prática
    // é constante por cliente, então max(...) só serve pra pegar o valor sem
    // precisar incluir as colunas no GROUP BY. Fica null até a nota ser
    // enriquecida (ver ClienteService.upsertFromWinThor/upsertFromXml).
    @Query("""
        select cn.cliente as cliente,
               max(cn.cidade) as cidade,
               count(distinct cn.carga.id) as quantidadeCargas,
               max(cn.carga.dtSaida) as ultimaCargaEm,
               max(cli.documento) as documento,
               max(cli.logradouro) as logradouro,
               max(cli.numero) as numero,
               max(cli.complemento) as complemento,
               max(cli.bairro) as bairro,
               max(cli.uf) as uf,
               max(cli.cep) as cep
        from CargaNota cn
        left join cn.clienteRef cli
        where cn.carga.rota.codigo = :codigoRota
        group by cn.cliente
        order by max(cn.cidade) asc nulls last, count(distinct cn.carga.id) desc, cn.cliente asc
        """)
    List<ClienteHistoricoRotaProjection> buscarClientesHistoricoPorRota(@Param("codigoRota") String codigoRota);

    // Cidade do cliente, independente da rota que a carga usou (a mesma cidade
    // pode ter sido atendida por rotas diferentes ao longo do tempo).
    @Query(value = """
        select cn.cidade as cidade,
               count(distinct cn.cliente) as quantidadeClientes,
               count(distinct cn.carga.id) as quantidadeCargas
        from CargaNota cn
        where cn.cidade is not null
        group by cn.cidade
        order by cn.cidade asc
        """,
        countQuery = """
        select count(distinct cn.cidade)
        from CargaNota cn
        where cn.cidade is not null
        """)
    Page<CidadeResumoProjection> listarCidades(Pageable pageable);

    @Query("""
        select cn.cliente as cliente,
               cn.cidade as cidade,
               count(distinct cn.carga.id) as quantidadeCargas,
               max(cn.carga.dtSaida) as ultimaCargaEm,
               max(cli.documento) as documento,
               max(cli.logradouro) as logradouro,
               max(cli.numero) as numero,
               max(cli.complemento) as complemento,
               max(cli.bairro) as bairro,
               max(cli.uf) as uf,
               max(cli.cep) as cep
        from CargaNota cn
        left join cn.clienteRef cli
        where cn.cidade = :cidade
        group by cn.cliente, cn.cidade
        order by count(distinct cn.carga.id) desc, cn.cliente asc
        """)
    List<ClienteHistoricoRotaProjection> buscarClientesPorCidade(@Param("cidade") String cidade);
}
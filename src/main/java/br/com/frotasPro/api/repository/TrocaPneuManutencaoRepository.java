package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.TrocaPneuManutencao;
import br.com.frotasPro.api.domain.enums.LadoPneu;
import br.com.frotasPro.api.domain.enums.PosicaoPneu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TrocaPneuManutencaoRepository extends JpaRepository<TrocaPneuManutencao, UUID> {

    @Query("""
           select t
           from TrocaPneuManutencao t
           join t.manutencao m
           join m.caminhao c
           where c.codigo = :codigoCaminhao
           """)
    Page<TrocaPneuManutencao> findByCaminhaoCodigo(
            @Param("codigoCaminhao") String codigoCaminhao,
            Pageable pageable
    );

    @Query("""
           select t
           from TrocaPneuManutencao t
           join t.manutencao m
           join m.caminhao c
           where c.codigo = :codigoCaminhao
             and t.eixo.numero = :eixoNumero
             and t.lado = :lado
             and t.posicao = :posicao
           order by t.kmOdometro asc
           """)
    List<TrocaPneuManutencao> findHistoricoPosicao(
            @Param("codigoCaminhao") String codigoCaminhao,
            @Param("eixoNumero") Integer eixoNumero,
            @Param("lado") LadoPneu lado,
            @Param("posicao") PosicaoPneu posicao
    );

    @Query("""
       select t
       from TrocaPneuManutencao t
       join t.pneu p
       join t.eixo e
       join e.caminhao c
       where (:codigoCaminhao is null or c.codigo = :codigoCaminhao)
         and (:codigoPneu is null or p.codigo = :codigoPneu)
       order by p.codigo, c.codigo, e.numero, t.kmOdometro
       """)
    List<TrocaPneuManutencao> buscarHistoricoPneus(
            @Param("codigoCaminhao") String codigoCaminhao,
            @Param("codigoPneu") String codigoPneu
    );

}

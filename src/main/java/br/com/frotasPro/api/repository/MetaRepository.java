package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.CategoriaCaminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetaRepository extends JpaRepository <Meta, UUID>{

    Optional<Meta> findFirstByCaminhaoCodigoAndDataIncioLessThanEqualAndDataFimGreaterThanEqual(
            String caminhaoCodigo,
            LocalDate dataInicio,
            LocalDate dataFim
    );

    Optional<Meta> findFirstByCategoriaCodigoAndDataIncioLessThanEqualAndDataFimGreaterThanEqual(
            String categoriaCodigo,
            LocalDate dataInicio,
            LocalDate dataFim
    );

    List<Meta> findByCaminhaoCodigoAndStatusMetaAndDataIncioLessThanEqualAndDataFimGreaterThanEqual(
            String caminhaoCodigo,
            StatusMeta statusMeta,
            LocalDate dataInicio,
            LocalDate dataFim
    );

    List<Meta> findByCategoriaCodigoAndStatusMetaAndDataIncioLessThanEqualAndDataFimGreaterThanEqual(
            String categoriaCodigo,
            StatusMeta statusMeta,
            LocalDate dataInicio,
            LocalDate dataFim
    );

    List<Meta> findByDataFimBeforeAndStatusMeta(LocalDate data, StatusMeta statusMeta);

    boolean existsByTipoMetaAndStatusMetaAndDataIncioAndCaminhaoAndCategoriaAndMotorista(
            TipoMeta tipoMeta,
            StatusMeta statusMeta,
            LocalDate dataIncio,
            Caminhao caminhao,
            CategoriaCaminhao categoria,
            Motorista motorista
    );

    @Query("""
       select (count(m) > 0)
       from Meta m
       where m.tipoMeta = :tipoMeta
         and m.statusMeta in :status
         and m.dataIncio <= :dataFim
         and m.dataFim >= :dataInicio
         and (
              (:caminhao is not null and m.caminhao = :caminhao)
              or (:categoria is not null and m.categoria = :categoria)
              or (:motorista is not null and m.motorista = :motorista)
         )
         and (:id is null or m.id <> :id)
       """)
    boolean existsMetaAtivaConflitante(
            @Param("tipoMeta") TipoMeta tipoMeta,
            @Param("status") List<StatusMeta> status,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("caminhao") Caminhao caminhao,
            @Param("categoria") CategoriaCaminhao categoria,
            @Param("motorista") Motorista motorista,
            @Param("id") UUID id
    );

    @Query("""
       select m
       from Meta m
       left join m.caminhao c
       left join m.motorista mot
       where m.tipoMeta = :tipoMeta
         and m.statusMeta = :status
         and m.dataIncio <= :data and m.dataFim >= :data
         and (
              (:caminhaoCodigo is not null and c.codigo = :caminhaoCodigo)
              or (:motoristaCodigo is not null and mot.codigo = :motoristaCodigo)
         )
       """)
    List<Meta> buscarMetasAtivasPorAlvoEData(
            @Param("tipoMeta") TipoMeta tipoMeta,
            @Param("status") StatusMeta status,
            @Param("data") LocalDate data,
            @Param("caminhaoCodigo") String caminhaoCodigo,
            @Param("motoristaCodigo") String motoristaCodigo
    );


    @Query("""
           select m
           from Meta m
           where (:caminhaoCodigo is null or m.caminhao.codigo = :caminhaoCodigo)
             and (:categoriaCodigo is null or m.categoria.codigo = :categoriaCodigo)
             and (:motoristaCodigo is null or m.motorista.codigo = :motoristaCodigo)
             and m.dataIncio >= :inicio
             and m.dataFim <= :fim
           order by m.dataIncio desc
           """)
    List<Meta> historicoMetas(
            @Param("caminhaoCodigo") String caminhaoCodigo,
            @Param("categoriaCodigo") String categoriaCodigo,
            @Param("motoristaCodigo") String motoristaCodigo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    Optional<Meta> findFirstByTipoMetaAndMotoristaAndStatusMetaOrderByDataIncioDesc(
            TipoMeta tipoMeta,
            Motorista motorista,
            StatusMeta statusMeta
    );

    Optional<Meta> findFirstByTipoMetaAndCaminhaoAndStatusMetaOrderByDataIncioDesc(
            TipoMeta tipoMeta,
            Caminhao caminhao,
            StatusMeta statusMeta
    );

    Optional<Meta> findFirstByTipoMetaAndCategoriaAndStatusMetaOrderByDataIncioDesc(
            TipoMeta tipoMeta,
            CategoriaCaminhao categoria,
            StatusMeta statusMeta
    );

    @Query("""
        select count(m)
        from Meta m
        where m.statusMeta = :status
          and m.dataIncio <= :data
          and m.dataFim >= :data
    """)
    long countMetasAtivas(@Param("status") StatusMeta status, @Param("data") LocalDate data);

    @Query("""
      select m
      from Meta m
      where m.statusMeta = :status
        and m.caminhao.codigo = :codigo
        and m.dataIncio <= :dataReferencia
        and m.dataFim >= :dataReferencia
    """)
    Optional<Meta> buscarMetaAtivaCaminhaoNaData(
            @Param("codigo") String codigo,
            @Param("dataReferencia") LocalDate dataReferencia,
            @Param("status") StatusMeta status
    );

    default Optional<Meta> buscarMetaAtivaCaminhaoNaData(String codigo, LocalDate dataReferencia) {
        return buscarMetaAtivaCaminhaoNaData(codigo, dataReferencia, StatusMeta.EM_ANDAMENTO);
    }


}

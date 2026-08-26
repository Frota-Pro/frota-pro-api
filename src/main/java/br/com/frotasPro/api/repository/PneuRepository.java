package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Pneu;
import br.com.frotasPro.api.domain.enums.StatusPneu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PneuRepository extends JpaRepository<Pneu, UUID> {
    Optional<Pneu> findByCodigo(String codigo);

    Page<Pneu> findByStatus(StatusPneu status, Pageable pageable);

    Page<Pneu> findByCodigoContainingIgnoreCaseOrNumeroSerieContainingIgnoreCaseOrMarcaContainingIgnoreCaseOrModeloContainingIgnoreCaseOrMedidaContainingIgnoreCase(
            String c1, String c2, String c3, String c4, String c5,
            Pageable pageable
    );

    /**
     * Pneus instalados no momento num caminhão cujo código, código externo ou
     * placa bate (parcial, sem acento/case) com o texto buscado. Usado pelo
     * filtro "por caminhão" da tela de Pneus — o pneu em si não guarda o
     * caminhão, só sabe através de tb_pneu_instalacao_atual.
     */
    @Query(
            value = """
                    select p.* from tb_pneu p
                    join tb_pneu_instalacao_atual ia on ia.pneu_id = p.id
                    join tb_caminhao c on c.id = ia.caminhao_id
                    where lower(c.codigo) like lower(concat('%', :caminhao, '%'))
                       or lower(coalesce(c.codigo_externo, '')) like lower(concat('%', :caminhao, '%'))
                       or lower(replace(coalesce(c.placa, ''), '-', '')) like lower(concat('%', replace(:caminhao, '-', ''), '%'))
                    """,
            countQuery = """
                    select count(*) from tb_pneu p
                    join tb_pneu_instalacao_atual ia on ia.pneu_id = p.id
                    join tb_caminhao c on c.id = ia.caminhao_id
                    where lower(c.codigo) like lower(concat('%', :caminhao, '%'))
                       or lower(coalesce(c.codigo_externo, '')) like lower(concat('%', :caminhao, '%'))
                       or lower(replace(coalesce(c.placa, ''), '-', '')) like lower(concat('%', replace(:caminhao, '-', ''), '%'))
                    """,
            nativeQuery = true
    )
    Page<Pneu> findByCaminhaoAtualMatch(@Param("caminhao") String caminhao, Pageable pageable);
}

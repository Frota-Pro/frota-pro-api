package br.com.frotasPro.api.modules.frota.repository;

import br.com.frotasPro.api.modules.frota.domain.Eixo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EixoRepository extends JpaRepository<Eixo, UUID> {

    Optional<Eixo> findByCaminhaoIdAndNumero(UUID caminhaoId, Integer numero);

    Page<Eixo> findByCaminhaoCodigo(String codigoCaminhao, Pageable pageable);

    @Query(value = "select exists (select 1 from tb_pneu where eixo_id = :eixoId)", nativeQuery = true)
    boolean existsPneuVinculadoAoEixo(@Param("eixoId") UUID eixoId);
}

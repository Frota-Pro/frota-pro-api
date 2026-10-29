package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Rota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RotaRepository extends JpaRepository<Rota, UUID> {

    Page<Rota> findAll(Pageable pageable);

    Optional<Rota> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    Optional<Rota> findByCidadeInicio(String cidadeInicio);

    /** Delete direto: o do JPA cascateia pras cargas da rota (Rota.cargas), e aqui elas já foram movidas. */
    @Modifying(flushAutomatically = true)
    @Query("delete from Rota r where r.id = :id")
    void apagarSemCascata(@Param("id") UUID id);

}

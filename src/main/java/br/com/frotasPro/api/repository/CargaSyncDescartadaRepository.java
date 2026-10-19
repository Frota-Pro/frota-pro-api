package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.CargaSyncDescartada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CargaSyncDescartadaRepository extends JpaRepository<CargaSyncDescartada, UUID> {

    Optional<CargaSyncDescartada> findByNumeroCargaExterno(String numeroCargaExterno);

    List<CargaSyncDescartada> findAllByOrderByUltimaOcorrenciaEmDesc();

    @Modifying
    @Query("delete from CargaSyncDescartada c where c.numeroCargaExterno = :numeroCargaExterno")
    int deletarPorNumeroCargaExterno(@Param("numeroCargaExterno") String numeroCargaExterno);
}

package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.AnexoParada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnexoParadaRepository extends JpaRepository<AnexoParada, UUID> {

    List<AnexoParada> findByParadaId(UUID paradaId);
}

package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.DespesaParada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DespesaParadaRepository extends JpaRepository<DespesaParada, UUID> {
}

package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.ParadaCarga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ParadaCargaRepository extends JpaRepository<ParadaCarga, UUID> {
}

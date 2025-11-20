package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.GrupoConta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GrupoContaRepository extends JpaRepository<GrupoConta, UUID> {
}

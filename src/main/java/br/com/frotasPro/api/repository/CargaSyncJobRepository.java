package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.CargaSyncJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CargaSyncJobRepository extends JpaRepository<CargaSyncJob, UUID> {
}

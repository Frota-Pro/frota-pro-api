package br.com.frotasPro.api.repository.integracao;

import br.com.frotasPro.api.domain.integracao.CargaSyncJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CargaSyncJobRepository extends JpaRepository<CargaSyncJob, UUID> {
}

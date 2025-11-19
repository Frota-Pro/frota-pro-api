package br.com.frotasPro.api.repository.integracao;

import br.com.frotasPro.api.domain.integracao.CaminhaoSyncJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CaminhaoSyncJobRepository extends JpaRepository<CaminhaoSyncJob, UUID> {
}

package br.com.frotasPro.api.repository.integracao;

import br.com.frotasPro.api.domain.enums.StatusSincronizacao;
import br.com.frotasPro.api.domain.integracao.CargaSyncJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface CargaSyncJobRepository extends JpaRepository<CargaSyncJob, UUID> {

    Page<CargaSyncJob> findByEmpresaIdAndStatusInOrderByCriadoEmDesc(
            UUID empresaId,
            Collection<StatusSincronizacao> status,
            Pageable pageable
    );
}

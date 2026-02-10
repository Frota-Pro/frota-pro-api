package br.com.frotasPro.api.repository.integracao;

import br.com.frotasPro.api.domain.enums.StatusSincronizacao;
import br.com.frotasPro.api.domain.integracao.MotoristaSyncJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface MotoristaSyncJobRepository extends JpaRepository<MotoristaSyncJob, UUID> {

    Page<MotoristaSyncJob> findByEmpresaIdAndStatusInOrderByCriadoEmDesc(
            UUID empresaId,
            Collection<StatusSincronizacao> status,
            Pageable pageable
    );
}

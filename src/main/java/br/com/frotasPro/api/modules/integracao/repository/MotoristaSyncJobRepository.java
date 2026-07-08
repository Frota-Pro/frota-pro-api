package br.com.frotasPro.api.modules.integracao.repository;

import br.com.frotasPro.api.modules.integracao.domain.MotoristaSyncJob;
import br.com.frotasPro.api.shared.enums.StatusSincronizacao;

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

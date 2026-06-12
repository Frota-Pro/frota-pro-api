package br.com.frotasPro.api.modules.frota.repository;

import br.com.frotasPro.api.modules.frota.domain.DocumentoCaminhao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentoCaminhaoRepository extends JpaRepository<DocumentoCaminhao, UUID> {

    Page<DocumentoCaminhao> findByCaminhaoId(UUID caminhaoId, Pageable pageable);
}

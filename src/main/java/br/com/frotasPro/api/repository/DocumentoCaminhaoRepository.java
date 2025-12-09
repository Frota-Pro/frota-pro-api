package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.DocumentoCaminhao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentoCaminhaoRepository extends JpaRepository<DocumentoCaminhao, UUID> {

    List<DocumentoCaminhao> findByCaminhaoId(UUID caminhaoId);
}

package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.DocumentoMotorista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentoMotoristaRepository extends JpaRepository<DocumentoMotorista, UUID> {

    List<DocumentoMotorista> findByMotoristaId(UUID motoristaId);
}

package br.com.frotasPro.api.modules.manutencao.repository;

import br.com.frotasPro.api.modules.manutencao.domain.DocumentoManutencao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentoManutencaoRepository extends JpaRepository<DocumentoManutencao, UUID> {

    Page<DocumentoManutencao> findByManutencaoCodigo(String codigo, Pageable pageable);
}

package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.MultaAnexo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MultaAnexoRepository extends JpaRepository<MultaAnexo, UUID> {

    List<MultaAnexo> findByMultaIdOrderByCriadoEmDesc(UUID multaId);
}

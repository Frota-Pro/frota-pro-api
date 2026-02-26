package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.MetaCategoriaCaminhaoVinculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MetaCategoriaCaminhaoVinculoRepository extends JpaRepository<MetaCategoriaCaminhaoVinculo, UUID> {
    void deleteByMetaId(UUID metaId);
}

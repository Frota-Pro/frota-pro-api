package br.com.frotasPro.api.modules.meta.repository;

import br.com.frotasPro.api.modules.meta.domain.MetaCategoriaCaminhaoVinculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MetaCategoriaCaminhaoVinculoRepository extends JpaRepository<MetaCategoriaCaminhaoVinculo, UUID> {
    void deleteByMetaId(UUID metaId);

    List<MetaCategoriaCaminhaoVinculo> findByMetaId(UUID metaId);
}

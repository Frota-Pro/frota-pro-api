package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.PlanejamentoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanejamentoItemRepository extends JpaRepository<PlanejamentoItem, UUID> {

    List<PlanejamentoItem> findByPlanilhaIdOrderByNumeroCarregamento(UUID planilhaId);

    List<PlanejamentoItem> findByPlanilhaIdIn(Collection<UUID> planilhaIds);

    Optional<PlanejamentoItem> findByPlanilhaIdAndNumeroCarregamento(UUID planilhaId, Integer numeroCarregamento);
}

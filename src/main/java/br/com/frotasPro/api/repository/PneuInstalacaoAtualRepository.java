package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.PneuInstalacaoAtual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PneuInstalacaoAtualRepository extends JpaRepository<PneuInstalacaoAtual, UUID> {
    Optional<PneuInstalacaoAtual> findByPneu_Codigo(String codigoPneu);
    List<PneuInstalacaoAtual> findAllByCaminhaoId(UUID caminhaoId);
    void deleteByPneu_Codigo(String codigoPneu);
}

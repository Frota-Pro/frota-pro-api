package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.PneuMovimentacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PneuMovimentacaoRepository extends JpaRepository<PneuMovimentacao, UUID> {
    Page<PneuMovimentacao> findByPneu_CodigoOrderByDataEventoDesc(String codigoPneu, Pageable pageable);
    Optional<PneuMovimentacao> findTopByPneu_CodigoOrderByDataEventoDesc(String codigoPneu);
}

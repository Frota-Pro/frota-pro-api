package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.PlanoManutencaoPreventiva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanoManutencaoPreventivaRepository extends JpaRepository<PlanoManutencaoPreventiva, UUID> {

    Page<PlanoManutencaoPreventiva> findByCaminhaoCodigo(String codigoCaminhao, Pageable pageable);

    Page<PlanoManutencaoPreventiva> findByAtivo(boolean ativo, Pageable pageable);

    // O cálculo de "está vencendo" depende de km (não dá pra comparar direto
    // no banco com o odômetro do caminhão de forma simples), então busca só
    // os candidatos ativos/não notificados e filtra em Java.
    List<PlanoManutencaoPreventiva> findByAtivoTrueAndNotificadoVencimentoEmIsNull();
}

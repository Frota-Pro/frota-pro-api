package br.com.frotasPro.api.util;

import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalcularMediaKmLitroService {

    private final AbastecimentoRepository repository;

    public BigDecimal calcular(Caminhao caminhao, LocalDateTime dtAbastecimento, Integer kmAtual, BigDecimal litros) {
        return calcular(caminhao, null, dtAbastecimento, kmAtual, litros);
    }

    public BigDecimal calcular(Caminhao caminhao,
                               UUID ignorarAbastecimentoId,
                               LocalDateTime dtAbastecimento,
                               Integer kmAtual,
                               BigDecimal litros) {

        if (kmAtual == null || litros == null || litros.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        Optional<Abastecimento> ultimo;

        if (dtAbastecimento != null) {
            // pega o último abastecimento ANTERIOR ao atual (ordem cronológica)
            ultimo = (ignorarAbastecimentoId != null)
                    ? repository.findFirstByCaminhaoIdAndDtAbastecimentoLessThanAndIdNotOrderByDtAbastecimentoDesc(
                    caminhao.getId(), dtAbastecimento, ignorarAbastecimentoId)
                    : repository.findFirstByCaminhaoIdAndDtAbastecimentoLessThanOrderByDtAbastecimentoDesc(
                    caminhao.getId(), dtAbastecimento);
        } else {
            // fallback (mantém comportamento antigo)
            ultimo = repository.findFirstByCaminhaoIdOrderByDtAbastecimentoDesc(caminhao.getId());
        }

        if (ultimo.isEmpty()) {
            return null;
        }

        Integer kmAnterior = ultimo.get().getKmOdometro();

        if (kmAnterior == null || kmAnterior <= 0) {
            return null;
        }

        int kmRodado = kmAtual - kmAnterior;

        if (kmRodado <= 0) {
            return null;
        }

        return BigDecimal.valueOf(kmRodado)
                .divide(litros, 3, RoundingMode.HALF_UP);
    }
}

package br.com.frotasPro.api.util;

import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CalcularMediaKmLitroService {

    private final AbastecimentoRepository repository;

    public BigDecimal calcular(Caminhao caminhao, Integer kmAtual, BigDecimal litros) {

        if (kmAtual == null || litros == null || litros.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        Optional<Abastecimento> ultimo =
                repository.findFirstByCaminhaoIdOrderByDtAbastecimentoDesc(caminhao.getId());

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

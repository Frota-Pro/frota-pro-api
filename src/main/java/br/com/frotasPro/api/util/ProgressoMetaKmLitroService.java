package br.com.frotasPro.api.util;

import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgressoMetaKmLitroService {

    private final MetaRepository metaRepository;
    private final AbastecimentoRepository abastecimentoRepository;
    private final CaminhaoRepository caminhaoRepository;

    public BigDecimal calcularProgressoKmLitroParaMeta(Meta meta) {

        if (meta.getCaminhao() == null) return null;

        UUID caminhaoId = meta.getCaminhao().getId();
        LocalDateTime inicio = meta.getDataIncio().atStartOfDay();
        LocalDateTime fim = meta.getDataFim().atTime(23, 59, 59);

        BigDecimal media = abastecimentoRepository
                .mediaKmLitroPorCaminhaoEPeriodo(caminhaoId, inicio, fim);

        return media; // esse é o valor "realizado" da meta KM/L
    }
}

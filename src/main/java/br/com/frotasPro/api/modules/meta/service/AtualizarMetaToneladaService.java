package br.com.frotasPro.api.modules.meta.service;

import br.com.frotasPro.api.modules.meta.domain.Meta;
import br.com.frotasPro.api.modules.meta.repository.MetaRepository;
import br.com.frotasPro.api.shared.enums.StatusMeta;
import br.com.frotasPro.api.shared.enums.TipoMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AtualizarMetaToneladaService {

    private final MetaRepository metaRepository;

    @Transactional
    public void registrarTonelada(String caminhaoCodigo, String motoristaCodigo,
                                  BigDecimal pesoCarga, LocalDate dataReferencia) {

        if (pesoCarga == null || pesoCarga.compareTo(BigDecimal.ZERO) <= 0)
            return;

        List<Meta> metas = metaRepository.buscarMetasAtivasPorAlvoEData(
                TipoMeta.TONELADA,
                StatusMeta.EM_ANDAMENTO,
                dataReferencia,
                caminhaoCodigo,
                motoristaCodigo
        );

        for (Meta meta : metas) {
            BigDecimal atual = meta.getValorRealizado() != null ? meta.getValorRealizado() : BigDecimal.ZERO;
            meta.setValorRealizado(atual.add(pesoCarga));
        }
    }
}


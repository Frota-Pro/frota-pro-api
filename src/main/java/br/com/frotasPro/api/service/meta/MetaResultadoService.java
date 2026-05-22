package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.MetaResultado;
import br.com.frotasPro.api.repository.MetaResultadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MetaResultadoService {

    private final MetaResultadoRepository repository;

    @Transactional
    public void registrar(Meta meta, Caminhao caminhao, BigDecimal valorRealizado, BigDecimal percentual, Boolean metaAtingida) {
        LocalDate inicio = meta != null ? meta.getDataIncio() : null;
        LocalDate fim = meta != null ? meta.getDataFim() : null;
        registrar(meta, caminhao, valorRealizado, percentual, metaAtingida, inicio, fim);
    }

    @Transactional
    public void registrar(Meta meta, Caminhao caminhao, BigDecimal valorRealizado, BigDecimal percentual, Boolean metaAtingida,
                          LocalDate periodoInicio, LocalDate periodoFim) {
        if (meta == null || meta.getId() == null || caminhao == null || caminhao.getId() == null) {
            return;
        }
        if (periodoInicio == null || periodoFim == null) {
            return;
        }

        MetaResultado resultado = MetaResultado.builder()
                .meta(meta)
                .caminhao(caminhao)
                .valorRealizado(valorRealizado)
                .periodoInicio(periodoInicio)
                .periodoFim(periodoFim)
                .percentual(percentual)
                .metaAtingida(Boolean.TRUE.equals(metaAtingida))
                .calculadoEm(LocalDateTime.now())
                .build();

        repository.save(resultado);
    }
}

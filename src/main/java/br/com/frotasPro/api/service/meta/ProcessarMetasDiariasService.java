package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcessarMetasDiariasService {

    private final MetaRepository metaRepository;

    @Transactional
    public void processar() {
        LocalDate hoje = LocalDate.now();


        List<Meta> metasVencidas = metaRepository
                .findByDataFimBeforeAndStatusMeta(hoje, StatusMeta.EM_ANDAMENTO);

        for (Meta meta : metasVencidas) {
            meta.setStatusMeta(StatusMeta.CONCLUIDA);
        }

        List<Meta> metasConcluidas = metaRepository
                .findByDataFimBeforeAndStatusMeta(hoje, StatusMeta.CONCLUIDA);

        for (Meta meta : metasConcluidas) {
            if (!meta.isRenovarAutomaticamente()) continue;

            Period periodo = Period.between(meta.getDataIncio(), meta.getDataFim());
            LocalDate novoInicio = meta.getDataFim().plusDays(1);
            LocalDate novoFim = novoInicio.plus(periodo);

            boolean jaExiste = metaRepository.existsMetaAtivaConflitante(
                    meta.getTipoMeta(),
                    List.of(StatusMeta.EM_ANDAMENTO, StatusMeta.NAO_INICIADA),
                    novoInicio,
                    novoFim,
                    meta.getCaminhao(),
                    meta.getCategoria(),
                    meta.getMotorista(),
                    null
            );

            if (jaExiste) {
                continue;
            }

            Meta nova = new Meta();
            nova.setDataIncio(novoInicio);
            nova.setDataFim(novoFim);
            nova.setTipoMeta(meta.getTipoMeta());
            nova.setValorMeta(meta.getValorMeta());
            nova.setValorRealizado(BigDecimal.ZERO);
            nova.setUnidade(meta.getUnidade());
            nova.setStatusMeta(StatusMeta.EM_ANDAMENTO);
            nova.setDescricao(meta.getDescricao());
            nova.setCaminhao(meta.getCaminhao());
            nova.setCategoria(meta.getCategoria());
            nova.setMotorista(meta.getMotorista());
            nova.setRenovarAutomaticamente(meta.isRenovarAutomaticamente());

            metaRepository.save(nova);
        }
    }
}

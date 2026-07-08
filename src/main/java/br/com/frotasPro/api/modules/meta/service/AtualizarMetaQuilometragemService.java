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
public class AtualizarMetaQuilometragemService {

    private final MetaRepository metaRepository;

    @Transactional
    public void registrarQuilometragem(String caminhaoCodigo, String motoristaCodigo,
                                       Integer kmInicial, Integer kmFinal, LocalDate dataReferencia) {

        System.out.println(">>> registrarQuilometragem <<<");
        System.out.println("caminhaoCodigo = " + caminhaoCodigo);
        System.out.println("motoristaCodigo = " + motoristaCodigo);
        System.out.println("kmInicial = " + kmInicial + ", kmFinal = " + kmFinal);
        System.out.println("dataReferencia = " + dataReferencia);

        if (kmInicial == null || kmFinal == null) {
            System.out.println("kmInicial ou kmFinal nulos, saindo...");
            return;
        }

        int kmRodado = kmFinal - kmInicial;
        System.out.println("kmRodado = " + kmRodado);

        if (kmRodado <= 0) {
            System.out.println("kmRodado <= 0, saindo...");
            return;
        }

        List<Meta> metas = metaRepository.buscarMetasAtivasPorAlvoEData(
                TipoMeta.QUILOMETRAGEM,
                StatusMeta.EM_ANDAMENTO,
                dataReferencia,
                caminhaoCodigo,
                motoristaCodigo
        );

        System.out.println("metas encontradas = " + metas.size());
        for (Meta meta : metas) {
            System.out.println("Meta ID = " + meta.getId()
                    + ", tipo = " + meta.getTipoMeta()
                    + ", status = " + meta.getStatusMeta()
                    + ", caminhao = " + (meta.getCaminhao() != null ? meta.getCaminhao().getCodigo() : "null")
                    + ", motorista = " + (meta.getMotorista() != null ? meta.getMotorista().getCodigo() : "null")
                    + ", valorRealizado atual = " + meta.getValorRealizado());
        }

        for (Meta meta : metas) {
            BigDecimal atual = meta.getValorRealizado() != null ? meta.getValorRealizado() : BigDecimal.ZERO;
            meta.setValorRealizado(atual.add(BigDecimal.valueOf(kmRodado)));
            System.out.println("Meta ID = " + meta.getId()
                    + " novo valorRealizado = " + meta.getValorRealizado());
        }
    }
}



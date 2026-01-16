package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.MetaMapper;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.util.ProgressoMetaKmLitroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarMetaAtivaComProgressoService {

    private final MetaRepository metaRepository;
    private final ProgressoMetaKmLitroService progressoMetaKmLitroService;

    @Transactional(readOnly = true)
    public MetaResponse buscar(String codigoCaminhao, LocalDate dataReferencia) {

        var meta = metaRepository.buscarMetaAtivaCaminhaoNaData(codigoCaminhao, dataReferencia)
                .orElseThrow(() -> new ObjectNotFound(
                        "Nenhuma meta ativa para o caminhão " + codigoCaminhao + " na data " + dataReferencia
                ));

        return MetaMapper.toResponse(meta);
    }

}


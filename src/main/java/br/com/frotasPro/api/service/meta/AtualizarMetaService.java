package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.request.MetaRequest;
import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.CategoriaCaminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.MetaMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CategoriaCaminhaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AtualizarMetaService {

    private final MetaRepository metaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final CategoriaCaminhaoRepository categoriaCaminhaoRepository;
    private final MotoristaRepository motoristaRepository;

    @Transactional
    public MetaResponse atualizar(UUID id, MetaRequest request) {

        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Meta não encontrada para o id: " + id));

        if (meta.getStatusMeta() == StatusMeta.CONCLUIDA) {
            throw new IllegalStateException("Meta encerrada não pode mais ser alterada.");
        }

        boolean temCaminhao = temTexto(request.getCaminhao());
        boolean temCategoria = temTexto(request.getCategoria());
        boolean temMotorista = temTexto(request.getMotorista());

        int count = (temCaminhao ? 1 : 0) + (temCategoria ? 1 : 0) + (temMotorista ? 1 : 0);

        if (count == 0) {
            throw new IllegalArgumentException("Meta deve ser associada a um caminhão, categoria ou motorista.");
        }
        if (count > 1) {
            throw new IllegalArgumentException("Meta não pode ser associada a mais de um alvo ao mesmo tempo (caminhão, categoria ou motorista).");
        }

        meta.setDataIncio(request.getDataIncio());
        meta.setDataFim(request.getDataFim());
        meta.setTipoMeta(request.getTipoMeta());
        meta.setValorMeta(request.getValorMeta());
        meta.setValorRealizado(request.getValorRealizado());
        meta.setUnidade(request.getUnidade());
        meta.setStatusMeta(request.getStatusMeta());
        meta.setDescricao(request.getDescricao());

        meta.setCaminhao(null);
        meta.setCategoria(null);
        meta.setMotorista(null);

        if (temCaminhao) {
            Caminhao caminhao = caminhaoRepository.findByCodigo(request.getCaminhao())
                    .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado para o código: " + request.getCaminhao()));
            meta.setCaminhao(caminhao);
        } else if (temCategoria) {
            CategoriaCaminhao categoria = categoriaCaminhaoRepository.findByCodigo(request.getCategoria())
                    .orElseThrow(() -> new ObjectNotFound("Categoria de caminhão não encontrada para o código: " + request.getCategoria()));
            meta.setCategoria(categoria);
        } else {
            Motorista motorista = motoristaRepository.findByCodigo(request.getMotorista())
                    .orElseThrow(() -> new ObjectNotFound("Motorista não encontrado para o código: " + request.getMotorista()));
            meta.setMotorista(motorista);
        }

        Meta salva = metaRepository.save(meta);
        return MetaMapper.toResponse(salva);
    }

    private boolean temTexto(String s) {
        return s != null && !s.trim().isEmpty();
    }
}

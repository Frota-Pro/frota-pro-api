package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.request.MetaRequest;
import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.CategoriaCaminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.MetaMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CategoriaCaminhaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.util.MetaProgressoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AtualizarMetaService {

    private final MetaRepository metaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final CategoriaCaminhaoRepository categoriaCaminhaoRepository;
    private final MotoristaRepository motoristaRepository;
    private final MetaProgressoService metaProgressoService;

    @Transactional
    public MetaResponse atualizar(UUID id, MetaRequest request) {

        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Meta não encontrada para o id: " + id));

        if (meta.getStatusMeta() == StatusMeta.CONCLUIDA) {
            throw new IllegalStateException("Meta encerrada não pode mais ser alterada.");
        }

        validarPeriodo(request);

        boolean temCaminhao = temTexto(request.getCaminhao());
        boolean temCategoria = temTexto(request.getCategoria());
        boolean temMotorista = temTexto(request.getMotorista());

        int count = (temCaminhao ? 1 : 0) + (temCategoria ? 1 : 0) + (temMotorista ? 1 : 0);

        if (count == 0) {
            if (request.getStatusMeta() != StatusMeta.CANCELADA) {
                throw new BusinessException("Para desvincular a meta, use status CANCELADA e não informe alvo.");
            }
        }
        if (count > 1) {
            throw new BusinessException("Informe apenas um alvo: caminhão OU categoria OU motorista.");
        }

        meta.setDataIncio(request.getDataIncio());
        meta.setDataFim(request.getDataFim());
        meta.setTipoMeta(request.getTipoMeta());
        meta.setValorMeta(request.getValorMeta());
        meta.setValorRealizado(request.getValorRealizado());
        meta.setUnidade(request.getUnidade());
        meta.setStatusMeta(request.getStatusMeta());
        meta.setDescricao(request.getDescricao());
        if (request.getRenovarAutomaticamente() != null) {
            meta.setRenovarAutomaticamente(Boolean.TRUE.equals(request.getRenovarAutomaticamente()));
        }

        meta.setCaminhao(null);
        meta.setCategoria(null);
        meta.setMotorista(null);

        Caminhao caminhao = null;
        CategoriaCaminhao categoria = null;
        Motorista motorista = null;

        if (temCaminhao) {
            caminhao = caminhaoRepository.findByCodigo(request.getCaminhao())
                    .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado para o código: " + request.getCaminhao()));
            meta.setCaminhao(caminhao);
        } else if (temCategoria) {
            categoria = categoriaCaminhaoRepository.findByCodigo(request.getCategoria())
                    .orElseThrow(() -> new ObjectNotFound("Categoria de caminhão não encontrada para o código: " + request.getCategoria()));
            meta.setCategoria(categoria);
        } else if (temMotorista) {
            motorista = motoristaRepository.findByCodigo(request.getMotorista())
                    .orElseThrow(() -> new ObjectNotFound("Motorista não encontrado para o código: " + request.getMotorista()));
            meta.setMotorista(motorista);
        }

        if (count == 1) {
            validarDuplicidade(meta, caminhao, categoria, motorista, meta.getId());
        }

        if (count == 1) {
            boolean recalcular = Boolean.TRUE.equals(request.getRecalcularProgresso());
            if (recalcular || request.getValorRealizado() == null) {
                BigDecimal recalculado = metaProgressoService.calcularValorRealizado(meta, caminhao, motorista);
                meta.setValorRealizado(recalculado);
            }
        }

        Meta salva = metaRepository.save(meta);
        return MetaMapper.toResponse(salva);
    }

    private boolean temTexto(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private void validarPeriodo(MetaRequest request) {
        if (request.getDataIncio() != null
                && request.getDataFim() != null
                && request.getDataIncio().isAfter(request.getDataFim())) {
            throw new BusinessException("Período inválido: data de início não pode ser maior que a data fim.");
        }
    }

    private void validarDuplicidade(Meta meta, Caminhao caminhao, CategoriaCaminhao categoria, Motorista motorista, UUID id) {
        if (meta.getStatusMeta() != StatusMeta.EM_ANDAMENTO && meta.getStatusMeta() != StatusMeta.NAO_INICIADA) {
            return;
        }
        boolean existe = metaRepository.existsMetaAtivaConflitante(
                meta.getTipoMeta(),
                List.of(StatusMeta.EM_ANDAMENTO, StatusMeta.NAO_INICIADA),
                meta.getDataIncio(),
                meta.getDataFim(),
                caminhao,
                categoria,
                motorista,
                id
        );
        if (existe) {
            throw new BusinessException("Já existe uma meta desse tipo ativa para o alvo informado no período.");
        }
    }
}

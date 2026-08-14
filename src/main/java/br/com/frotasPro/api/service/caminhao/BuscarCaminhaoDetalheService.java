package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.util.FusoHorarioUtils;

import br.com.frotasPro.api.controller.response.CaminhaoDetalheResponse;
import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.StatusManutencao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CaminhaoMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.repository.MovimentacaoSemCargaRepository;
import br.com.frotasPro.api.service.meta.BuscarMetaAtivaComProgressoService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarCaminhaoDetalheService {

    private final CaminhaoRepository caminhaoRepository;
    private final CargaRepository cargaRepository;
    private final AbastecimentoRepository abastecimentoRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final MovimentacaoSemCargaRepository movimentacaoSemCargaRepository;
    private final BuscarMetaAtivaComProgressoService buscarMetaAtivaComProgressoService;

    // Faltava esse @Cacheable — todo write path (controller e sync do WinThor)
    // já evita "caminhao_detalhes" religiosamente, mas nada aqui produzia o
    // cache; a eviction era um no-op silencioso e esse endpoint (6+ queries,
    // algumas com SUM) nunca foi cacheado de fato.
    @Cacheable("caminhao_detalhes")
    @Transactional(readOnly = true)
    public CaminhaoDetalheResponse detalhes(String codigo) {

        var caminhao = caminhaoRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Caminhão não encontrado: " + codigo));

        CaminhaoResponse base = CaminhaoMapper.toResponse(caminhao);

        long totalCargas = cargaRepository.countByCaminhaoCodigoOuCodigoExterno(codigo);
        long cargasFinalizadas = cargaRepository.countByCaminhaoCodigoOuCodigoExternoAndStatus(codigo, Status.FINALIZADA);

        BigDecimal litros = abastecimentoRepository.sumLitrosPorCaminhaoCodigoOuCodigoExterno(codigo);
        BigDecimal valor = abastecimentoRepository.sumValorPorCaminhaoCodigoOuCodigoExterno(codigo);

        BigDecimal peso = cargaRepository.sumPesoPorCaminhaoCodigoOuCodigoExterno(codigo);
        Long kmSemCarga = movimentacaoSemCargaRepository.sumKmPorCaminhaoCodigoOuCodigoExterno(codigo);

        long osAbertas = manutencaoRepository.countAbertasPorCaminhaoCodigo(
                codigo,
                List.of(StatusManutencao.AGENDADA, StatusManutencao.EM_ANDAMENTO)
        );

        List<MetaResponse> metasAtivas;
        try {
            metasAtivas = buscarMetaAtivaComProgressoService.buscar(codigo, FusoHorarioUtils.hojeBrasil());
        } catch (ObjectNotFound e) {
            metasAtivas = Collections.emptyList();
        }

        return new CaminhaoDetalheResponse(
                base,
                totalCargas,
                cargasFinalizadas,
                litros,
                valor,
                peso,
                kmSemCarga == null ? 0L : kmSemCarga,
                osAbertas,
                metasAtivas
        );
    }
}

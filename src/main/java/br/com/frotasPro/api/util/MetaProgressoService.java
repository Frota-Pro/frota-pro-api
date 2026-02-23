package br.com.frotasPro.api.util;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MetaProgressoService {

    private final CargaRepository cargaRepository;
    private final AbastecimentoRepository abastecimentoRepository;

    public BigDecimal calcularValorRealizado(Meta meta, Caminhao caminhaoReferencia, Motorista motoristaReferencia) {
        if (meta == null || meta.getTipoMeta() == null) {
            return null;
        }

        LocalDate inicio = meta.getDataIncio();
        LocalDate fim = meta.getDataFim();
        if (inicio == null || fim == null) {
            return meta.getValorRealizado();
        }

        Caminhao caminhao = caminhaoReferencia != null ? caminhaoReferencia : meta.getCaminhao();
        Motorista motorista = motoristaReferencia != null ? motoristaReferencia : meta.getMotorista();

        TipoMeta tipo = meta.getTipoMeta();
        if (tipo == TipoMeta.CONSUMO_COMBUSTIVEL) {
            LocalDateTime dtInicio = inicio.atStartOfDay();
            LocalDateTime dtFim = fim.atTime(23, 59, 59);
            if (motorista != null) {
                BigDecimal media = abastecimentoRepository
                        .mediaKmLitroPonderadaPorMotoristaEPeriodo(motorista.getId(), dtInicio, dtFim);
                return media != null ? media : BigDecimal.ZERO;
            }
            if (caminhao != null) {
                BigDecimal media = abastecimentoRepository
                        .mediaKmLitroPonderadaPorCaminhaoEPeriodo(caminhao.getId(), dtInicio, dtFim);
                return media != null ? media : BigDecimal.ZERO;
            }
            return meta.getValorRealizado();
        }

        if (tipo == TipoMeta.QUILOMETRAGEM) {
            Long total = null;
            if (caminhao != null) {
                total = cargaRepository.sumKmRodadoPorCaminhaoNoPeriodo(caminhao.getCodigo(), inicio, fim, Status.FINALIZADA);
            } else if (motorista != null) {
                total = cargaRepository.sumKmRodadoPorMotoristaNoPeriodo(motorista.getCodigo(), inicio, fim, Status.FINALIZADA);
            }
            return total != null ? BigDecimal.valueOf(total) : meta.getValorRealizado();
        }

        if (tipo == TipoMeta.TONELADA) {
            BigDecimal total = null;
            if (caminhao != null) {
                total = cargaRepository.sumPesoPorCaminhaoNoPeriodo(caminhao.getCodigo(), inicio, fim, Status.FINALIZADA);
            } else if (motorista != null) {
                total = cargaRepository.sumPesoPorMotoristaNoPeriodo(motorista.getCodigo(), inicio, fim, Status.FINALIZADA);
            }
            return total != null ? total : meta.getValorRealizado();
        }

        if (tipo == TipoMeta.CARGA_TRANSPORTADA) {
            Long total = null;
            if (caminhao != null) {
                total = cargaRepository.countCargasPorCaminhaoNoPeriodo(caminhao.getCodigo(), inicio, fim, Status.FINALIZADA);
            } else if (motorista != null) {
                total = cargaRepository.countCargasPorMotoristaNoPeriodo(motorista.getCodigo(), inicio, fim, Status.FINALIZADA);
            }
            return total != null ? BigDecimal.valueOf(total) : meta.getValorRealizado();
        }

        return meta.getValorRealizado();
    }
}

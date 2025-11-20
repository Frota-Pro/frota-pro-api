package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.response.CargaResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BuscarCargaService {

    private final CargaRepository cargaRepository;

    @Transactional(readOnly = true)
    public CargaResponse porCodigo(String NumeroCarga) {
        Carga carga = cargaRepository.findByNumeroCarga(NumeroCarga.trim())
                .orElseThrow(() -> new ObjectNotFound(
                        "Carga não encontrada para o código: " + NumeroCarga
                ));

        return CargaMapper.toResponse(carga);
    }

    @Transactional(readOnly = true)
    public CargaResponse porCodigoExterno(String codigoExterno) {
        Carga carga = cargaRepository.findByNumeroCargaExterno(codigoExterno.trim())
                .orElseThrow(() -> new ObjectNotFound(
                        "Carga não encontrada para o código externo: " + codigoExterno
                ));

        return CargaMapper.toResponse(carga);
    }

    @Transactional(readOnly = true)
    public Page<CargaResponse> porDataSaida(LocalDate dataSaida, Pageable pageable) {
        return cargaRepository.findByDtSaida(dataSaida, pageable)
                .map(CargaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CargaResponse> porPeriodoSaida(LocalDate inicio,
                                               LocalDate fim,
                                               Pageable pageable) {

        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("As datas de início e fim devem ser informadas.");
        }

        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("A data de início não pode ser maior que a data fim.");
        }

        return cargaRepository.findByDtSaidaBetween(inicio, fim, pageable)
                .map(CargaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CargaResponse> porPeriodoCriacao(LocalDateTime inicio,
                                                 LocalDateTime fim,
                                                 Pageable pageable) {

        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("As datas de início e fim devem ser informadas.");
        }

        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("A data de início não pode ser maior que a data fim.");
        }

        return cargaRepository.findByCriadoEmBetween(inicio, fim, pageable)
                .map(CargaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CargaResponse> porMotorista(String codigo, Pageable pageable) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("O código do motorista deve ser informado.");
        }

        String valor = codigo.trim();

        return cargaRepository
                .findByMotoristaCodigoOuCodigoExterno(valor, pageable)
                .map(CargaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CargaResponse> porCaminhao(String codigo, Pageable pageable) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("O código do caminhão deve ser informado.");
        }

        String valor = codigo.trim();

        return cargaRepository
                .findByCaminhaoCodigoOuCodigoExterno(valor, pageable)
                .map(CargaMapper::toResponse);
    }

}

package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.request.MarcarTransferenciaCargaRequest;
import br.com.frotasPro.api.modules.logistica.dto.response.CargaResponse;
import br.com.frotasPro.api.modules.logistica.domain.Carga;
import br.com.frotasPro.api.modules.logistica.domain.CargaTransferencia;
import br.com.frotasPro.api.modules.logistica.mapper.CargaMapper;
import br.com.frotasPro.api.modules.logistica.repository.CargaRepository;
import br.com.frotasPro.api.modules.logistica.repository.CargaTransferenciaRepository;
import br.com.frotasPro.api.shared.enums.StatusTransferenciaCarga;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarcarTransferenciaCargaService {

    private final CargaRepository cargaRepository;
    private final CargaTransferenciaRepository cargaTransferenciaRepository;

    @Transactional
    public CargaResponse marcar(String numeroCargaOrigem, MarcarTransferenciaCargaRequest request) {
        Carga origem = cargaRepository.findByNumeroCarga(numeroCargaOrigem.trim())
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada: " + numeroCargaOrigem));

        Carga destino = buscarDestino(request);
        if (destino != null && origem.getId().equals(destino.getId())) {
            throw new IllegalArgumentException("Carga origem e destino devem ser diferentes");
        }

        origem.setTransferenciaPendente(true);
        origem.setStatusTransferencia(StatusTransferenciaCarga.PENDENTE_SYNC);

        CargaTransferencia transferencia = new CargaTransferencia();
        transferencia.setCargaOrigem(origem);
        transferencia.setCargaDestino(destino);
        transferencia.setNumeroCargaOrigem(origem.getNumeroCarga());
        transferencia.setNumeroCargaDestino(destino != null ? destino.getNumeroCarga() : null);
        transferencia.setNumeroCargaExternoOrigem(origem.getNumeroCargaExterno());
        transferencia.setNumeroCargaExternoDestino(destino != null ? destino.getNumeroCargaExterno() : null);
        transferencia.setStatus(StatusTransferenciaCarga.PENDENTE_SYNC);
        transferencia.setTotalNotas(0);

        cargaTransferenciaRepository.save(transferencia);

        return CargaMapper.toResponse(cargaRepository.save(origem));
    }

    private Carga buscarDestino(MarcarTransferenciaCargaRequest request) {
        if (request == null || request.getNumeroCargaDestino() == null || request.getNumeroCargaDestino().trim().isEmpty()) {
            return null;
        }

        String numeroDestino = request.getNumeroCargaDestino().trim();
        return cargaRepository.findByNumeroCarga(numeroDestino)
                .orElseThrow(() -> new ObjectNotFound("Carga destino não encontrada: " + numeroDestino));
    }
}

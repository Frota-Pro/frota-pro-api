package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.request.TransferirNotasCargaRequest;
import br.com.frotasPro.api.controller.response.TransferirNotasCargaResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.CargaNota;
import br.com.frotasPro.api.domain.CargaTransferencia;
import br.com.frotasPro.api.domain.CargaTransferenciaNota;
import br.com.frotasPro.api.domain.enums.StatusTransferenciaCarga;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.CargaTransferenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TransferirNotasCargaService {

    private final CargaRepository cargaRepository;
    private final CargaTransferenciaRepository cargaTransferenciaRepository;

    @Transactional
    public TransferirNotasCargaResponse transferir(String numeroCargaOrigem, TransferirNotasCargaRequest request) {
        String origemNumero = normalizarObrigatorio(numeroCargaOrigem, "Carga origem é obrigatória");
        String destinoNumero = normalizarObrigatorio(request.getNumeroCargaDestino(), "Carga destino é obrigatória");

        if (origemNumero.equals(destinoNumero)) {
            throw new IllegalArgumentException("Carga origem e destino devem ser diferentes");
        }

        Carga origem = buscarCargaComNotas(origemNumero);
        Carga destino = buscarCargaComNotas(destinoNumero);

        Map<String, TransferirNotasCargaRequest.NotaTransferenciaRequest> notasSolicitadas = normalizarNotasSolicitadas(request);
        Map<String, CargaNota> notasOrigem = indexarNotas(origem.getNotas());

        List<String> ausentes = notasSolicitadas.keySet()
                .stream()
                .filter(key -> !notasOrigem.containsKey(key))
                .toList();

        if (!ausentes.isEmpty()) {
            throw new IllegalArgumentException("Notas não encontradas na carga origem: " + String.join(", ", ausentes));
        }

        Set<String> clientesTransferidos = new LinkedHashSet<>();
        Set<String> notasDestino = indexarNotas(destino.getNotas()).keySet();
        List<CargaNota> notasParaRemover = new ArrayList<>();

        for (String key : notasSolicitadas.keySet()) {
            CargaNota notaOrigem = notasOrigem.get(key);
            String cliente = notaOrigem.getCliente();
            String nota = notaOrigem.getNota();

            clientesTransferidos.add(cliente);
            notasParaRemover.add(notaOrigem);

            if (!notasDestino.contains(key)) {
                CargaNota novaNota = new CargaNota();
                novaNota.setCarga(destino);
                novaNota.setCliente(cliente);
                novaNota.setNota(nota);
                destino.getNotas().add(novaNota);
                notasDestino.add(key);
            }
        }

        origem.getNotas().removeAll(notasParaRemover);
        atualizarOrdemEntrega(origem, destino, clientesTransferidos);
        marcarTransferenciaPendente(origem);
        registrarTransferencia(origem, destino, notasParaRemover);

        Carga origemSalva = cargaRepository.save(origem);
        Carga destinoSalva = cargaRepository.save(destino);

        return TransferirNotasCargaResponse.builder()
                .totalNotasTransferidas(notasSolicitadas.size())
                .cargaOrigem(CargaMapper.toResponse(origemSalva))
                .cargaDestino(CargaMapper.toResponse(destinoSalva))
                .build();
    }

    private void marcarTransferenciaPendente(Carga origem) {
        origem.setTransferenciaPendente(true);
        origem.setStatusTransferencia(StatusTransferenciaCarga.PENDENTE_SYNC);
    }

    private void registrarTransferencia(Carga origem, Carga destino, List<CargaNota> notasTransferidas) {
        CargaTransferencia transferencia = new CargaTransferencia();
        transferencia.setCargaOrigem(origem);
        transferencia.setCargaDestino(destino);
        transferencia.setNumeroCargaOrigem(origem.getNumeroCarga());
        transferencia.setNumeroCargaDestino(destino.getNumeroCarga());
        transferencia.setNumeroCargaExternoOrigem(origem.getNumeroCargaExterno());
        transferencia.setNumeroCargaExternoDestino(destino.getNumeroCargaExterno());
        transferencia.setStatus(StatusTransferenciaCarga.PENDENTE_SYNC);
        transferencia.setTotalNotas(notasTransferidas.size());

        for (CargaNota nota : notasTransferidas) {
            CargaTransferenciaNota historicoNota = new CargaTransferenciaNota();
            historicoNota.setTransferencia(transferencia);
            historicoNota.setCliente(nota.getCliente());
            historicoNota.setNota(nota.getNota());
            transferencia.getNotas().add(historicoNota);
        }

        cargaTransferenciaRepository.save(transferencia);
    }

    private Carga buscarCargaComNotas(String numeroCarga) {
        return cargaRepository.findByNumeroCargaWithNotas(numeroCarga)
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada: " + numeroCarga));
    }

    private Map<String, TransferirNotasCargaRequest.NotaTransferenciaRequest> normalizarNotasSolicitadas(
            TransferirNotasCargaRequest request
    ) {
        if (request.getNotas() == null || request.getNotas().isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos uma nota para transferência");
        }

        Map<String, TransferirNotasCargaRequest.NotaTransferenciaRequest> notas = new LinkedHashMap<>();
        for (TransferirNotasCargaRequest.NotaTransferenciaRequest item : request.getNotas()) {
            String cliente = normalizarObrigatorio(item.getCliente(), "Cliente da nota é obrigatório");
            String nota = normalizarObrigatorio(item.getNota(), "Número da nota é obrigatório");
            String key = notaKey(cliente, nota);

            if (notas.containsKey(key)) {
                throw new IllegalArgumentException("Nota duplicada na solicitação: " + key);
            }

            item.setCliente(cliente);
            item.setNota(nota);
            notas.put(key, item);
        }

        return notas;
    }

    private Map<String, CargaNota> indexarNotas(List<CargaNota> notas) {
        Map<String, CargaNota> index = new LinkedHashMap<>();
        if (notas == null) {
            return index;
        }

        for (CargaNota nota : notas) {
            index.put(notaKey(nota.getCliente(), nota.getNota()), nota);
        }
        return index;
    }

    private void atualizarOrdemEntrega(Carga origem, Carga destino, Set<String> clientesTransferidos) {
        Set<String> clientesRestantesOrigem = new LinkedHashSet<>();
        for (CargaNota nota : origem.getNotas()) {
            clientesRestantesOrigem.add(nota.getCliente());
        }

        origem.getOrdemEntregaClientes().removeIf(cliente -> !clientesRestantesOrigem.contains(cliente));

        for (String cliente : clientesTransferidos) {
            if (!destino.getOrdemEntregaClientes().contains(cliente)) {
                destino.getOrdemEntregaClientes().add(cliente);
            }
        }
    }

    private static String normalizarObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor.trim();
    }

    private static String notaKey(String cliente, String nota) {
        return normalizarObrigatorio(cliente, "Cliente da nota é obrigatório")
                + "||"
                + normalizarObrigatorio(nota, "Número da nota é obrigatório");
    }
}

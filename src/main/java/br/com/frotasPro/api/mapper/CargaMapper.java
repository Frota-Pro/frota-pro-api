package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.request.CargaRequest;
import br.com.frotasPro.api.controller.response.CargaMinResponse;
import br.com.frotasPro.api.controller.response.CargaResponse;
import br.com.frotasPro.api.controller.response.ClienteCargaResponse;
import br.com.frotasPro.api.controller.response.NotaFiscalArquivoResponse;
import br.com.frotasPro.api.domain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.*;

public class CargaMapper {

    public static Carga toEntity(CargaRequest request,
                                 Motorista motorista,
                                 Caminhao caminhao,
                                 Rota rota,
                                 List<Ajudante> ajudantes) {

        Carga carga = new Carga();
        carga.setDtSaida(request.getDtSaida());
        carga.setDtPrevista(request.getDtPrevista());
        carga.setDtChegada(request.getDtChegada());
        carga.setPesoCarga(request.getPesoCarga());
        carga.setValorTotal(request.getValorTotal());
        carga.setKmInicial(request.getKmInicial());
        carga.setKmFinal(request.getKmFinal());
        carga.setStatusCarga(request.getStatusCarga());

        carga.setMotorista(motorista);
        carga.setCaminhao(caminhao);
        carga.setRota(rota);
        carga.setAjudantes(ajudantes);

        return carga;
    }

    public static void updateEntity(Carga carga,
                                    CargaRequest request,
                                    Motorista motorista,
                                    Caminhao caminhao,
                                    Rota rota,
                                    List<Ajudante> ajudantes) {
        carga.setDtSaida(request.getDtSaida());
        carga.setDtPrevista(request.getDtPrevista());
        carga.setDtChegada(request.getDtChegada());
        carga.setPesoCarga(request.getPesoCarga());
        carga.setKmFinal(request.getKmFinal());
        carga.setKmInicial(request.getKmInicial());
        carga.setKmFinal(request.getKmFinal());
        carga.setStatusCarga(request.getStatusCarga());

        carga.setMotorista(motorista);
        carga.setCaminhao(caminhao);
        carga.setRota(rota);
        carga.setAjudantes(ajudantes);
    }

    public static CargaResponse toResponse(Carga carga) {
        return CargaResponse.builder()
                .id(carga.getId())
                .numeroCarga(carga.getNumeroCarga())
                .numeroCargaExterno(carga.getNumeroCargaExterno())
                .dtSaida(carga.getDtSaida())
                .dtPrevista(carga.getDtPrevista())
                .dtChegada(carga.getDtChegada())
                .pesoCarga(carga.getPesoCarga())
                .valorTotal(carga.getValorTotal())
                .kmInicial(carga.getKmInicial())
                .kmFinal(carga.getKmFinal())
                .kmTotal(carga.calcularKmTotal())
                .diasAtraso(carga.calcularAtraso())
                .clientes(
                        carga.getNotas().stream()
                                .collect(groupingBy(CargaNota::getCliente))
                                .entrySet()
                                .stream()
                                .map(entry -> ClienteCargaResponse.builder()
                                        .cliente(entry.getKey())
                                        .cidade(
                                                entry.getValue().stream()
                                                        .map(CargaNota::getCidade)
                                                        .filter(Objects::nonNull)
                                                        .findFirst()
                                                        .orElse(null)
                                        )
                                        .notas(
                                                entry.getValue().stream()
                                                        .map(CargaNota::getNota)
                                                        .toList()
                                        )
                                        .build()
                                )
                                .toList()
                )
                .notasComArquivo(
                        carga.getNotas().stream()
                                .filter(n -> n.getArquivo() != null)
                                .map(n -> NotaFiscalArquivoResponse.builder()
                                        .cliente(n.getCliente())
                                        .nota(n.getNota())
                                        .arquivoId(n.getArquivo().getId())
                                        .nomeArquivo(n.getArquivo().getNomeOriginal())
                                        .urlDownload("/arquivos/" + n.getArquivo().getId() + "/download")
                                        .build()
                                )
                                .toList()
                )
                .statusCarga(carga.getStatusCarga())
                .transferenciaPendente(carga.isTransferenciaPendente())
                .statusTransferencia(carga.getStatusTransferencia())
                .codigoMotorista(carga.getMotorista().getCodigo())
                .nomeMotorista(carga.getMotorista().getNome())
                .codigoCaminhao(carga.getCaminhao().getCodigo())
                .placaCaminhao(carga.getCaminhao().getPlaca())
                .codigoRota(carga.getRota().getCodigo())
                .codigosAjudantes(
                        carga.getAjudantes()
                                .stream()
                                .map(Ajudante::getCodigo)
                                .toList()
                )
                .ordemEntregaClientes(new ArrayList<>(carga.getOrdemEntregaClientes()))
                .clientesNaoRoteirizados(new ArrayList<>(carga.getClientesNaoRoteirizados()))
                .observacaoMotorista(carga.getObservacaoMotorista())
                .motoristaDefinidoManualmente(carga.isMotoristaDefinidoManualmente())
                .codigosDevolucaoEncontrados(
                        carga.getCodigosDevolucaoEncontrados() != null && !carga.getCodigosDevolucaoEncontrados().isBlank()
                                ? Arrays.stream(carga.getCodigosDevolucaoEncontrados().split(","))
                                        .map(String::trim)
                                        .filter(s -> !s.isBlank())
                                        .toList()
                                : List.of()
                )
                .teveTransferencia(carga.isTeveTransferencia())
                .diminuicaoPesoValorBloqueada(carga.isDiminuicaoPesoValorBloqueada())
                .naoEncontradaNoWinThor(carga.isNaoEncontradaNoWinThor())
                .dataVerificacaoWinThor(carga.getDataVerificacaoWinThor())
                .build();
    }


    public static CargaMinResponse toMinResponse(Carga carga) {
        return CargaMinResponse.builder()
                .numeroCarga(carga.getNumeroCarga())
                .numeroCargaExterno(carga.getNumeroCargaExterno())
                .dtSaida(carga.getDtSaida())
                .pesoCarga(carga.getPesoCarga())
                .valorTotal(carga.getValorTotal())
                .statusCarga(carga.getStatusCarga())
                .transferenciaPendente(carga.isTransferenciaPendente())
                .statusTransferencia(carga.getStatusTransferencia())
                .nomeMotorista(carga.getMotorista().getNome())
                .placaCaminhao(carga.getCaminhao().getPlaca())
                .codigosDevolucaoEncontrados(
                        carga.getCodigosDevolucaoEncontrados() != null && !carga.getCodigosDevolucaoEncontrados().isBlank()
                                ? Arrays.stream(carga.getCodigosDevolucaoEncontrados().split(","))
                                        .map(String::trim)
                                        .filter(s -> !s.isBlank())
                                        .toList()
                                : List.of()
                )
                .teveTransferencia(carga.isTeveTransferencia())
                .diminuicaoPesoValorBloqueada(carga.isDiminuicaoPesoValorBloqueada())
                .naoEncontradaNoWinThor(carga.isNaoEncontradaNoWinThor())
                .build();
    }

    /**
     * Decide qual número de carga deve ser exibido ao usuário: o externo
     * (vindo da integração) tem precedência apenas quando a integração está
     * ativa E aquela carga específica já tem um número externo sincronizado;
     * caso contrário, sempre o número interno.
     */
    public static String resolverNumeroExibicao(String numeroCarga, String numeroCargaExterno, boolean integracaoAtiva) {
        if (integracaoAtiva && numeroCargaExterno != null && !numeroCargaExterno.isBlank()) {
            return numeroCargaExterno;
        }
        return numeroCarga;
    }

    public static void aplicarNumeroExibicao(CargaResponse response, Carga carga, boolean integracaoAtiva) {
        response.setNumeroCargaExibicao(
                resolverNumeroExibicao(carga.getNumeroCarga(), carga.getNumeroCargaExterno(), integracaoAtiva)
        );
    }

    public static void aplicarNumeroExibicao(CargaMinResponse response, Carga carga, boolean integracaoAtiva) {
        response.setNumeroCargaExibicao(
                resolverNumeroExibicao(carga.getNumeroCarga(), carga.getNumeroCargaExterno(), integracaoAtiva)
        );
    }

}

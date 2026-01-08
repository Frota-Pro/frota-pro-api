package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.request.CargaRequest;
import br.com.frotasPro.api.controller.response.CargaMinResponse;
import br.com.frotasPro.api.controller.response.CargaResponse;
import br.com.frotasPro.api.controller.response.ClienteCargaResponse;
import br.com.frotasPro.api.domain.*;

import java.util.List;

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
                                .collect(
                                        groupingBy(
                                                CargaNota::getCliente,
                                                mapping(CargaNota::getNota, toList())
                                        )
                                )
                                .entrySet()
                                .stream()
                                .map(entry -> ClienteCargaResponse.builder()
                                        .cliente(entry.getKey())
                                        .notas(entry.getValue())
                                        .build()
                                )
                                .toList()
                )
                .statusCarga(carga.getStatusCarga())
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
                .nomeMotorista(carga.getMotorista().getNome())
                .placaCaminhao(carga.getCaminhao().getPlaca())
                .build();

    }

}

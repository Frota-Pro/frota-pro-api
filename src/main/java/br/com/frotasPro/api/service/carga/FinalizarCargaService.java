package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.util.AtualizarMetaQuilometragemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FinalizarCargaService {

    private final CargaRepository cargaRepository;
    private final AtualizarMetaQuilometragemService atualizarMetaQuilometragemService;


    @Transactional
    public String finalizarCarga(String numCarga, Integer kmFinal) {

        Carga carga = cargaRepository.findByNumeroCarga(numCarga)
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada"));

        if (carga.getDtChegada() != null || carga.getKmFinal() != null) {
            return "Carga já está finalizada";
        }

        if (kmFinal == null || kmFinal <= 0) {
            throw new IllegalArgumentException("KM final inválido");
        }

        if (carga.getKmInicial() != null && kmFinal < carga.getKmInicial()) {
            throw new IllegalArgumentException("KM final não pode ser menor que o KM inicial");
        }

        if (carga.getStatusCarga() != Status.EM_ROTA) {
            return "Não é possível finalizar uma carga que não está EM_ROTA";
        }


        Motorista motorista = carga.getMotorista();
        if (motorista != null) {
            motorista.setStatus(Status.DISPONIVEL);
        }

        Caminhao caminhao = carga.getCaminhao();
        if(caminhao != null){
            caminhao.setStatus(Status.DISPONIVEL);
        }

        carga.setDtChegada(LocalDate.now());
        carga.setKmFinal(kmFinal);
        carga.setStatusCarga(Status.FINALIZADA);

        atualizarMetaQuilometragemService.registrarQuilometragem(
                carga.getCaminhao().getCodigo(),
                carga.getMotorista() != null ? carga.getMotorista().getCodigo() : null,
                carga.getKmInicial(),
                kmFinal,
                carga.getDtChegada()
        );


        cargaRepository.save(carga);
        return "Carga finalizada com sucesso! 🚚💨";
    }
}

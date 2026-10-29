package br.com.frotasPro.api.service.rota;

import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.excption.ConflictException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.RotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletarRotaService {

    private final RotaRepository repository;
    private final CargaRepository cargaRepository;

    @Transactional
    public void deletar(String codigo) {
        Rota rota = repository.findByCodigo(codigo.trim())
                .orElseThrow(() -> new ObjectNotFound("Rota não encontrada para o código: " + codigo));

        // Rota.cargas cascateia REMOVE: apagar uma rota com cargas apagaria as cargas junto.
        long cargas = cargaRepository.countByRotaId(rota.getId());
        if (cargas > 0) {
            throw new ConflictException("A rota " + rota.getCodigo() + " tem " + cargas
                    + " carga(s) e não pode ser excluída. Use \"Mesclar\" para passar as cargas para a rota certa.");
        }

        repository.delete(rota);
    }
}

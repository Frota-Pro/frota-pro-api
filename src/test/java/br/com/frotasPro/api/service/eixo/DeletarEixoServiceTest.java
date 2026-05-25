package br.com.frotasPro.api.service.eixo;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Eixo;
import br.com.frotasPro.api.excption.ConflictException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.EixoRepository;
import br.com.frotasPro.api.repository.PneuInstalacaoAtualRepository;
import br.com.frotasPro.api.repository.TrocaPneuManutencaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeletarEixoServiceTest {

    @Mock
    private EixoRepository eixoRepository;
    @Mock
    private TrocaPneuManutencaoRepository trocaPneuManutencaoRepository;
    @Mock
    private PneuInstalacaoAtualRepository pneuInstalacaoAtualRepository;

    private DeletarEixoService service;
    private Eixo eixo;

    @BeforeEach
    void setUp() {
        service = new DeletarEixoService(
                eixoRepository,
                trocaPneuManutencaoRepository,
                pneuInstalacaoAtualRepository
        );
        Caminhao caminhao = new Caminhao();
        caminhao.setId(UUID.randomUUID());
        eixo = Eixo.builder()
                .id(UUID.randomUUID())
                .numero(2)
                .caminhao(caminhao)
                .build();
    }

    @Test
    void excluiEixoSemVinculoOperacional() {
        when(eixoRepository.findById(eixo.getId())).thenReturn(Optional.of(eixo));

        service.deletar(eixo.getId());

        verify(eixoRepository).delete(eixo);
    }

    @Test
    void retornaNotFoundQuandoEixoNaoExiste() {
        UUID id = UUID.randomUUID();
        when(eixoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.deletar(id));

        verify(eixoRepository, never()).delete(org.mockito.ArgumentMatchers.any(Eixo.class));
    }

    @Test
    void bloqueiaExclusaoQuandoEixoFoiUsadoEmTrocaDePneu() {
        when(eixoRepository.findById(eixo.getId())).thenReturn(Optional.of(eixo));
        when(trocaPneuManutencaoRepository.existsByEixoId(eixo.getId())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.deletar(eixo.getId()));

        verify(eixoRepository, never()).delete(eixo);
    }

    @Test
    void bloqueiaExclusaoQuandoExistePneuInstaladoNaPosicaoDoEixo() {
        when(eixoRepository.findById(eixo.getId())).thenReturn(Optional.of(eixo));
        when(pneuInstalacaoAtualRepository.existsByCaminhaoIdAndEixoNumero(
                eixo.getCaminhao().getId(), eixo.getNumero())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.deletar(eixo.getId()));

        verify(eixoRepository, never()).delete(eixo);
    }
}

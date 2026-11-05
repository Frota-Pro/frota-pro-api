package br.com.frotasPro.api.service.linhatransporte;

import br.com.frotasPro.api.controller.request.LinhaTransporteRequest;
import br.com.frotasPro.api.domain.LinhaTransporte;
import br.com.frotasPro.api.excption.ConflictException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.LinhaTransporteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinhaTransporteServiceTest {

    @Mock
    private LinhaTransporteRepository repository;
    @Mock
    private CaminhaoRepository caminhaoRepository;

    private LinhaTransporteService service;

    @BeforeEach
    void setUp() {
        service = new LinhaTransporteService(repository, caminhaoRepository);
    }

    private static LinhaTransporteRequest pedido(String nome, Boolean ativo) {
        LinhaTransporteRequest r = new LinhaTransporteRequest();
        r.setNome(nome);
        r.setAtivo(ativo);
        return r;
    }

    private static LinhaTransporte linha(String nome) {
        LinhaTransporte l = new LinhaTransporte();
        l.setId(UUID.randomUUID());
        l.setNome(nome);
        return l;
    }

    @Test
    void criaLinhaAtivaComONomeSemEspacosNasPontas() {
        when(repository.existsByNomeIgnoreCase("Frios")).thenReturn(false);
        when(repository.save(any(LinhaTransporte.class))).thenAnswer(i -> i.getArgument(0));

        var resposta = service.criar(pedido("  Frios ", null));

        assertEquals("Frios", resposta.getNome());
        assertTrue(resposta.isAtivo());
    }

    @Test
    void nomeRepetidoSemDiferenciarMaiusculaEConflito() {
        when(repository.existsByNomeIgnoreCase("FRIOS")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.criar(pedido("FRIOS", null)));
        verify(repository, never()).save(any());
    }

    @Test
    void renomearParaNomeDeOutraLinhaEConflito() {
        LinhaTransporte seca = linha("Linha seca");
        when(repository.findById(seca.getId())).thenReturn(Optional.of(seca));
        when(repository.existsByNomeIgnoreCaseAndIdNot("Frios", seca.getId())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.atualizar(seca.getId(), pedido("Frios", null)));
    }

    @Test
    void atualizarMudaONomeEPodeInativar() {
        LinhaTransporte frios = linha("Frios");
        when(repository.findById(frios.getId())).thenReturn(Optional.of(frios));
        when(repository.existsByNomeIgnoreCaseAndIdNot("Congelados", frios.getId())).thenReturn(false);
        when(repository.save(frios)).thenReturn(frios);

        var resposta = service.atualizar(frios.getId(), pedido("Congelados", false));

        assertEquals("Congelados", resposta.getNome());
        assertFalse(resposta.isAtivo());
    }

    @Test
    void atualizarSemInformarAtivoMantemOQueEstava() {
        LinhaTransporte frios = linha("Frios");
        when(repository.findById(frios.getId())).thenReturn(Optional.of(frios));
        when(repository.save(frios)).thenReturn(frios);

        assertTrue(service.atualizar(frios.getId(), pedido("Frios", null)).isAtivo());
    }

    @Test
    void linhaInexistenteNaoAtualizaNemApaga() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.atualizar(id, pedido("Frios", null)));
        assertThrows(ObjectNotFound.class, () -> service.deletar(id));
    }

    @Test
    void linhaEmUsoPorCaminhaoNaoPodeSerApagada() {
        LinhaTransporte frios = linha("Frios");
        when(repository.findById(frios.getId())).thenReturn(Optional.of(frios));
        when(caminhaoRepository.existsByLinhaTransporteId(frios.getId())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.deletar(frios.getId()));
        verify(repository, never()).delete(any());
    }

    @Test
    void linhaSemCaminhaoPodeSerApagada() {
        LinhaTransporte frios = linha("Frios");
        when(repository.findById(frios.getId())).thenReturn(Optional.of(frios));
        when(caminhaoRepository.existsByLinhaTransporteId(frios.getId())).thenReturn(false);

        service.deletar(frios.getId());

        verify(repository).delete(frios);
    }

    @Test
    void listaNaOrdemDoBanco() {
        when(repository.findAllByOrderByNomeAsc()).thenReturn(List.of(linha("Frios"), linha("Linha seca")));

        assertEquals(List.of("Frios", "Linha seca"), service.listar().stream().map(r -> r.getNome()).toList());
    }
}

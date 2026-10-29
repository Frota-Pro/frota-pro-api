package br.com.frotasPro.api.service.parametrosistema;

import br.com.frotasPro.api.controller.request.ParametroSistemaUpdateRequest;
import br.com.frotasPro.api.controller.response.ParametroSistemaResponse;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.repository.ParametroSistemaRepository;
import br.com.frotasPro.api.service.usuario.UsuarioAutenticadoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParametroSistemaServiceSegurancaTest {

    @Mock
    private ParametroSistemaRepository repository;
    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    private ParametroSistemaService service;
    private ParametroSistema parametro;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        service = new ParametroSistemaService(repository, usuarioAutenticadoService);
        UUID empresa = UUID.randomUUID();
        ReflectionTestUtils.setField(service, "empresaIdPadrao", empresa);

        parametro = new ParametroSistema();
        parametro.setEmpresaId(empresa);
        lenient().when(repository.findByEmpresaId(empresa)).thenReturn(Optional.of(parametro));
        lenient().when(repository.save(any(ParametroSistema.class))).thenAnswer(i -> i.getArgument(0));

        admin = new Usuario();
        admin.setLogin("admin");
        lenient().when(usuarioAutenticadoService.getUsuario()).thenReturn(admin);
    }

    private ParametroSistemaUpdateRequest requisicao(Boolean bloquear, Boolean vincular) {
        ParametroSistemaUpdateRequest r = new ParametroSistemaUpdateRequest();
        r.setDiasAntecedenciaVencimentoDocumento(5);
        r.setKmAntecedenciaManutencaoPreventiva(500);
        r.setDiasManutencaoEstagnada(7);
        r.setDiasAntecedenciaPrazoMulta(5);
        r.setValidarMotivoAlteracaoPesoValorCarga(false);
        r.setPermitirAtualizacaoPorTransferencia(true);
        r.setValidarTempoMinimoCarga(false);
        r.setTempoMinimoEntregaPadraoMinutos(30);
        r.setDiasRetencaoAuditoria(180);
        r.setPercentualLimiteAnomaliaAbastecimento(25);
        r.setBloquearSenhaPadrao(bloquear);
        r.setVincularDispositivo(vincular);
        return r;
    }

    @Test
    void nascemDesligados() {
        ParametroSistemaResponse resposta = service.buscar();

        assertFalse(resposta.isBloquearSenhaPadrao());
        assertFalse(resposta.isVincularDispositivo());
    }

    @Test
    void ligaEDesligaPelaTela() {
        admin.setSenhaTemporaria(false);

        ParametroSistemaResponse ligado = service.atualizar(requisicao(true, true));
        assertTrue(ligado.isBloquearSenhaPadrao());
        assertTrue(ligado.isVincularDispositivo());
        assertTrue(parametro.isBloquearSenhaPadrao());
        assertTrue(parametro.isVincularDispositivo());

        ParametroSistemaResponse desligado = service.atualizar(requisicao(false, false));
        assertFalse(desligado.isBloquearSenhaPadrao());
        assertFalse(desligado.isVincularDispositivo());
    }

    @Test
    void clienteAntigoSemOsCamposNaoMudaNada() {
        parametro.setBloquearSenhaPadrao(true);
        parametro.setVincularDispositivo(true);

        service.atualizar(requisicao(null, null));

        assertTrue(parametro.isBloquearSenhaPadrao());
        assertTrue(parametro.isVincularDispositivo());
    }

    @Test
    void naoLigaOBloqueioSeAPropriaContaDeQuemLigaAindaUsaSenhaPadrao() {
        admin.setSenhaTemporaria(true);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.atualizar(requisicao(true, null)));

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        assertTrue(erro.getReason().contains("Troque a sua própria senha"));
        assertFalse(parametro.isBloquearSenhaPadrao());
        verify(repository, never()).save(any());
    }

    @Test
    void desligarOBloqueioNuncaExigeNada() {
        parametro.setBloquearSenhaPadrao(true);
        admin.setSenhaTemporaria(true);

        service.atualizar(requisicao(false, null));

        assertFalse(parametro.isBloquearSenhaPadrao());
    }

    @Test
    void ligarOVinculoNaoDependeDaSenhaDeQuemLiga() {
        admin.setSenhaTemporaria(true);

        service.atualizar(requisicao(null, true));

        assertTrue(parametro.isVincularDispositivo());
    }
}

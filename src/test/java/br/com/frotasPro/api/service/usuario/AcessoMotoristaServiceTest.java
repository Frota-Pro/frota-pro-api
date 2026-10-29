package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcessoMotoristaServiceTest {

    @Mock
    private CargaRepository cargaRepository;
    @Mock
    private MotoristaRepository motoristaRepository;
    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;
    @InjectMocks
    private AcessoMotoristaService service;

    private final UUID meuUsuario = UUID.randomUUID();
    private final UUID outroUsuario = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        lenient().when(usuarioAutenticadoService.getUsuarioIdLogado()).thenReturn(meuUsuario);
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    private void logadoComo(String... perfis) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "fulano", "n/a", Arrays.stream(perfis).map(SimpleGrantedAuthority::new).toList()));
    }

    private Motorista motoristaDe(UUID usuarioId) {
        Motorista m = new Motorista();
        m.setId(UUID.randomUUID());
        if (usuarioId != null) {
            Usuario u = new Usuario();
            u.setId(usuarioId);
            m.setUsuario(u);
        }
        return m;
    }

    private Carga cargaDe(Motorista motorista) {
        Carga c = new Carga();
        c.setNumeroCarga("CAR-000362");
        c.setMotorista(motorista);
        return c;
    }

    @Test
    void soEhRestritoQuemTemSoOPerfilDeMotorista() {
        logadoComo("ROLE_MOTORISTA");
        assertTrue(service.ehSomenteMotorista());

        logadoComo("ROLE_MOTORISTA", "ROLE_OPERADOR_LOGISTICA");
        assertFalse(service.ehSomenteMotorista());

        logadoComo("ROLE_ADMIN");
        assertFalse(service.ehSomenteMotorista());
    }

    @Test
    void motoristaAcessaACargaDele() {
        logadoComo("ROLE_MOTORISTA");
        when(cargaRepository.findByNumeroCarga("CAR-000362")).thenReturn(Optional.of(cargaDe(motoristaDe(meuUsuario))));

        assertDoesNotThrow(() -> service.exigirCargaDoMotorista(" CAR-000362 "));
    }

    @Test
    void motoristaNaoAcessaACargaDeOutro() {
        logadoComo("ROLE_MOTORISTA");
        when(cargaRepository.findByNumeroCarga("CAR-000362")).thenReturn(Optional.of(cargaDe(motoristaDe(outroUsuario))));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.exigirCargaDoMotorista("CAR-000362"));
        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
    }

    @Test
    void cargaDeMotoristaSemUsuarioVinculadoFicaBloqueadaParaMotorista() {
        logadoComo("ROLE_MOTORISTA");

        assertThrows(ResponseStatusException.class, () -> service.exigirCargaDoMotorista(cargaDe(motoristaDe(null))));
    }

    @Test
    void escritorioNaoEhLimitadoENemConsultaOBanco() {
        logadoComo("ROLE_OPERADOR_LOGISTICA");

        assertDoesNotThrow(() -> service.exigirCargaDoMotorista("CAR-000362"));
        assertDoesNotThrow(() -> service.exigirMotoristaProprio(UUID.randomUUID()));
        verifyNoInteractions(cargaRepository, motoristaRepository);
    }

    @Test
    void motoristaSoMexeNosProprioDocumentos() {
        logadoComo("ROLE_MOTORISTA");
        Motorista eu = motoristaDe(meuUsuario);
        Motorista outro = motoristaDe(outroUsuario);
        when(motoristaRepository.findById(eu.getId())).thenReturn(Optional.of(eu));
        when(motoristaRepository.findById(outro.getId())).thenReturn(Optional.of(outro));

        assertDoesNotThrow(() -> service.exigirMotoristaProprio(eu.getId()));
        assertThrows(ResponseStatusException.class, () -> service.exigirMotoristaProprio(outro.getId()));
    }
}

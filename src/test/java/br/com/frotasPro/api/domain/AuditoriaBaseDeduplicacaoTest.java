package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.config.ApplicationContextProvider;
import br.com.frotasPro.api.service.auditoria.RegistrarLogAuditoriaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * A mesma mudança na mesma entidade não pode virar várias linhas na auditoria (chegou a 84 durante um sync):
 * depois de registrar, o "antes" passa a ser a foto recém-gravada.
 */
class AuditoriaBaseDeduplicacaoTest {

    static class EntidadeFalsa extends AuditoriaBase {
        private String nome;
        private Integer quantidade;
    }

    @AfterEach
    void limparContexto() {
        new ApplicationContextProvider().setApplicationContext(null);
    }

    private static void ligarContexto(RegistrarLogAuditoriaService service) {
        ApplicationContext contexto = mock(ApplicationContext.class);
        when(contexto.getBean(RegistrarLogAuditoriaService.class)).thenReturn(service);
        new ApplicationContextProvider().setApplicationContext(contexto);
    }

    private static void chamar(Object alvo, String metodo) throws Exception {
        Method m = AuditoriaBase.class.getDeclaredMethod(metodo);
        m.setAccessible(true);
        m.invoke(alvo);
    }

    private static void definir(Object alvo, String campo, Object valor) throws Exception {
        var f = EntidadeFalsa.class.getDeclaredField(campo);
        f.setAccessible(true);
        f.set(alvo, valor);
    }

    @Test
    void mesmaMudancaGravadaVariasVezesNaMesmaTransacaoViraUmaLinhaSo() throws Exception {
        RegistrarLogAuditoriaService service = mock(RegistrarLogAuditoriaService.class);
        {
            ligarContexto(service);

            EntidadeFalsa entidade = new EntidadeFalsa();
            definir(entidade, "nome", "antes");
            chamar(entidade, "capturarSnapshotAuditoria"); // como o @PostLoad

            definir(entidade, "nome", "depois");
            chamar(entidade, "auditarAtualizacao"); // 1º flush: mudou
            chamar(entidade, "auditarAtualizacao"); // 2º flush: nada mudou desde o último registro
            chamar(entidade, "auditarAtualizacao"); // 3º flush: idem

            verify(service, times(1)).registrar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());

            definir(entidade, "quantidade", 7); // mudança de verdade depois disso
            chamar(entidade, "auditarAtualizacao");

            verify(service, times(2)).registrar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Test
    void semMudancaNenhumaNaoRegistraNada() throws Exception {
        RegistrarLogAuditoriaService service = mock(RegistrarLogAuditoriaService.class);
        {
            ligarContexto(service);

            EntidadeFalsa entidade = new EntidadeFalsa();
            definir(entidade, "nome", "igual");
            chamar(entidade, "capturarSnapshotAuditoria");

            chamar(entidade, "auditarAtualizacao");
            chamar(entidade, "auditarAtualizacao");

            verify(service, times(0)).registrar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        }
    }
}

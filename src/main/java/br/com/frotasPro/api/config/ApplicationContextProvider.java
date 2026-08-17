package br.com.frotasPro.api.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Ponte pra código que roda fora do gerenciamento do Spring (ex: callbacks de
 * ciclo de vida JPA em entidades, que o Hibernate instancia diretamente, sem
 * injeção de dependência) conseguir pegar um bean quando precisar — usado
 * pela auditoria automática em {@link br.com.frotasPro.api.domain.AuditoriaBase}.
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> type) {
        return context != null ? context.getBean(type) : null;
    }
}

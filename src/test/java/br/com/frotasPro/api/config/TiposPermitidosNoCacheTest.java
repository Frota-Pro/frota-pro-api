package br.com.frotasPro.api.config;

import br.com.frotasPro.api.service.cache.CargaCachedPage;
import br.com.frotasPro.api.controller.response.CargaMinResponse;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TiposPermitidosNoCacheTest {

    private GenericJackson2JsonRedisSerializer serializerDeProducao() {
        // mesmo ponto de partida do Spring Boot (módulos de nomes de parâmetro, datas etc.)
        ObjectMapper mapper = org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json().build();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new com.fasterxml.jackson.databind.module.SimpleModule().addDeserializer(
                java.math.BigDecimal.class, new br.com.frotasPro.api.config.json.FlexibleBigDecimalDeserializer()));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(TiposPermitidosNoCache.validador(),
                ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Test
    void aceitaOsTiposDoProprioSistemaEOsBasicosDoJava() {
        for (String nome : List.of(
                "br.com.frotasPro.api.controller.response.CargaResponse",
                "br.com.frotasPro.api.controller.response.OficinaDashboardResponse$Item",
                "br.com.frotasPro.api.service.cache.CargaCachedPage",
                "br.com.frotasPro.api.domain.enums.Status",
                "java.util.ArrayList", "java.util.LinkedHashMap", "java.util.UUID",
                "java.util.ImmutableCollections$ListN", "java.util.Collections$UnmodifiableRandomAccessList",
                "java.time.LocalDate", "java.time.LocalDateTime", "java.time.OffsetDateTime",
                "java.math.BigDecimal", "java.lang.Long", "java.lang.Integer", "java.lang.Boolean")) {
            assertTrue(TiposPermitidosNoCache.permite(nome), nome);
        }
    }

    @Test
    void recusaQualquerOutraClasse() {
        for (String nome : List.of(
                "java.lang.ProcessBuilder", "java.lang.Runtime", "java.lang.Class",
                "javax.naming.InitialContext", "com.sun.rowset.JdbcRowSetImpl",
                "org.springframework.context.support.ClassPathXmlApplicationContext",
                "org.springframework.beans.factory.config.PropertyPathFactoryBean",
                "java.net.URL", "java.util.concurrent.ForkJoinPool", "java.io.File",
                "br.com.frotasPro.api.service.usuario.UsuarioService",
                "br.com.frotasPro.api.config.SecurityConfig",
                "br.com.frotasPro.api.controller.responseX.Evil",
                "br.com.frotasPro.apiEvil.Qualquer")) {
            assertFalse(TiposPermitidosNoCache.permite(nome), nome);
        }
        assertFalse(TiposPermitidosNoCache.permite(null));
    }

    @Test
    void valorEscritoNoRedisComClasseMaliciosaNaoEInstanciado() {
        GenericJackson2JsonRedisSerializer serializer = serializerDeProducao();
        byte[] malicioso = "[\"java.lang.ProcessBuilder\",{\"command\":[\"calc\"]}]".getBytes();
        byte[] malicioso2 = "{\"@class\":\"com.sun.rowset.JdbcRowSetImpl\",\"dataSourceName\":\"ldap://x/y\",\"autoCommit\":true}".getBytes();

        assertThrows(RuntimeException.class, () -> serializer.deserialize(malicioso));
        assertThrows(RuntimeException.class, () -> serializer.deserialize(malicioso2));
    }

    @Test
    void valoresReaisDoCacheContinuamFazendoIdaEVolta() {
        GenericJackson2JsonRedisSerializer serializer = serializerDeProducao();

        CargaMinResponse carga = CargaMinResponse.builder()
                .numeroCarga("CAR-000362")
                .dtSaida(java.time.LocalDate.of(2026, 10, 29))
                .valorTotal(new java.math.BigDecimal("64632.69"))
                .statusCarga(br.com.frotasPro.api.domain.enums.Status.values()[0])
                .codigosDevolucaoEncontrados(List.of("53"))
                .build();
        CargaCachedPage pagina = new CargaCachedPage(List.of(carga), 7);
        CargaCachedPage lido = (CargaCachedPage) serializer.deserialize(serializer.serialize(pagina));
        assertEquals(7, lido.totalElements());
        assertEquals("CAR-000362", lido.content().get(0).getNumeroCarga());
        assertEquals(java.time.LocalDate.of(2026, 10, 29), lido.content().get(0).getDtSaida());
        assertEquals(0, new java.math.BigDecimal("64632.69").compareTo(lido.content().get(0).getValorTotal()));
        assertEquals(carga.getStatusCarga(), lido.content().get(0).getStatusCarga());

        List<Object> lista = new ArrayList<>(List.of(1L, java.time.LocalDate.of(2026, 10, 29), new java.math.BigDecimal("12.50")));
        assertEquals(lista, serializer.deserialize(serializer.serialize(lista)));
    }

    /**
     * Varre tudo que o sistema coloca em cache (@Cacheable) e confere, campo a campo, se todas as
     * classes envolvidas passam pela lista fechada. Se alguém passar a cachear um tipo novo fora
     * da lista, este teste falha em vez de o cache quebrar só em produção.
     */
    @Test
    void todosOsTiposCacheadosPelaApiPassamNaListaFechada() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));

        Set<String> fora = new TreeSet<>();
        int metodosCacheados = 0;
        for (BeanDefinition def : scanner.findCandidateComponents("br.com.frotasPro.api")) {
            Class<?> classe = Class.forName(def.getBeanClassName());
            for (Method metodo : classe.getDeclaredMethods()) {
                if (metodo.isAnnotationPresent(Cacheable.class)) {
                    metodosCacheados++;
                    coletar(metodo.getGenericReturnType(), new HashSet<>(), fora, metodo.getDeclaringClass().getSimpleName()
                            + "." + metodo.getName());
                }
            }
        }

        assertTrue(metodosCacheados > 20, "nenhum método @Cacheable encontrado? " + metodosCacheados);
        assertTrue(fora.isEmpty(), "Tipos cacheados que a lista fechada recusaria: " + fora);
    }

    private void coletar(Type tipo, Set<Class<?>> visitados, Set<String> fora, String origem) {
        if (tipo instanceof ParameterizedType p) {
            coletar(p.getRawType(), visitados, fora, origem);
            for (Type argumento : p.getActualTypeArguments()) {
                coletar(argumento, visitados, fora, origem);
            }
        } else if (tipo instanceof WildcardType w) {
            for (Type limite : w.getUpperBounds()) {
                coletar(limite, visitados, fora, origem);
            }
        } else if (tipo instanceof GenericArrayType g) {
            coletar(g.getGenericComponentType(), visitados, fora, origem);
        } else if (tipo instanceof Class<?> classe) {
            if (classe.isArray()) {
                coletar(classe.getComponentType(), visitados, fora, origem);
                return;
            }
            if (classe.isPrimitive() || classe == void.class || classe == Void.class || !visitados.add(classe)) {
                return;
            }
            boolean ehDoSistema = classe.getName().startsWith("br.com.frotasPro.api.");
            boolean contenedorDoJava = classe.isInterface() && classe.getName().startsWith("java.util.");
            if (!contenedorDoJava && !TiposPermitidosNoCache.permite(classe.getName())) {
                fora.add(classe.getName() + " (em " + origem + ")");
            }
            if (ehDoSistema && !classe.isEnum()) {
                for (Class<?> c = classe; c != null && c != Object.class; c = c.getSuperclass()) {
                    for (Field campo : c.getDeclaredFields()) {
                        if (!Modifier.isStatic(campo.getModifiers())) {
                            coletar(campo.getGenericType(), visitados, fora, origem);
                        }
                    }
                }
            }
        }
    }
}

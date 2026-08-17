package br.com.frotasPro.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Tira uma "foto" (JSON) dos campos simples de uma entidade — usada pra
 * guardar o "antes" e o "depois" na trilha de auditoria. Só olha campos de
 * tipo simples (String, número, boolean, data, enum, UUID); nunca navega
 * pra dentro de uma associação (@ManyToOne/@OneToOne) nem de uma coleção
 * (@OneToMany/@ManyToMany) — decide isso só pelo TIPO DECLARADO do campo,
 * sem nunca chamar o getter, então nunca força o carregamento de um proxy
 * lazy do Hibernate. O preço é não mostrar o que mudou numa associação
 * (ex: qual caminhão foi vinculado), só nos campos "de verdade" da entidade.
 */
public class AuditoriaSnapshotSerializer {

    private static final String PACOTE_DOMINIO = "br.com.frotasPro.api.domain";

    private static final Set<String> CAMPOS_IGNORADOS = Set.of(
            "senha", "criadoPor", "criadoEm", "atualizadoPor", "atualizadoEm", "snapshotAuditoriaAnterior"
    );

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private AuditoriaSnapshotSerializer() {
    }

    public static String serializar(Object entidade) {
        if (entidade == null) {
            return null;
        }
        try {
            Map<String, Object> valores = new TreeMap<>();
            for (Class<?> clazz = entidade.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                        continue;
                    }
                    String nome = field.getName();
                    if (CAMPOS_IGNORADOS.contains(nome) || valores.containsKey(nome)) {
                        continue;
                    }

                    Class<?> tipo = field.getType();
                    if (Collection.class.isAssignableFrom(tipo) || Map.class.isAssignableFrom(tipo)) {
                        continue; // coleção — nunca toca, evita lazy loading
                    }
                    String pacoteTipo = tipo.getPackageName();
                    if (pacoteTipo.equals(PACOTE_DOMINIO) || pacoteTipo.startsWith(PACOTE_DOMINIO + ".")) {
                        continue; // associação pra outra entidade — nunca toca, evita lazy loading
                    }

                    Object valor;
                    try {
                        field.setAccessible(true);
                        valor = field.get(entidade);
                    } catch (Exception e) {
                        continue;
                    }

                    if (valor instanceof Enum<?> enumValor) {
                        valor = enumValor.name();
                    }
                    valores.put(nome, valor);
                }
            }
            return OBJECT_MAPPER.writeValueAsString(valores);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> desserializar(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}

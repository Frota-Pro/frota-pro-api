package br.com.frotasPro.api.config;

import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.util.regex.Pattern;

/**
 * Lista fechada das classes que o cache do Redis pode instanciar ao ler um valor.
 *
 * O cache grava o nome da classe junto com o JSON e, ao ler, cria a classe que estiver escrita.
 * Com um validador que aceita tudo (LaissezFaire), quem conseguisse escrever no Redis escolheria
 * qualquer classe do classpath e executaria código no servidor (desserialização insegura).
 * Aqui só passam os tipos que a própria API coloca em cache: as respostas e páginas em cache do
 * FrotaPRO, os enums dela e tipos básicos do Java (listas, mapas, datas, números).
 */
public final class TiposPermitidosNoCache {

    private static final Pattern PERMITIDOS = Pattern.compile(
            "br\\.com\\.frotasPro\\.api\\.(controller\\.response|service\\.cache|domain\\.enums)\\..+"
                    + "|java\\.util\\.(ArrayList|LinkedList|HashMap|LinkedHashMap|TreeMap|HashSet|LinkedHashSet|TreeSet|UUID"
                    + "|Collections\\$[A-Za-z]+|ImmutableCollections\\$[A-Za-z0-9]+|Arrays\\$ArrayList)"
                    + "|java\\.time\\.[A-Za-z]+"
                    + "|java\\.math\\.(BigDecimal|BigInteger)"
                    + "|java\\.lang\\.(Long|Integer|Short|Byte|Double|Float|Boolean|Character|String)"
    );

    private TiposPermitidosNoCache() {
    }

    public static boolean permite(String nomeDaClasse) {
        return nomeDaClasse != null && PERMITIDOS.matcher(nomeDaClasse).matches();
    }

    public static PolymorphicTypeValidator validador() {
        return BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(PERMITIDOS)
                .allowIfSubTypeIsArray()
                .build();
    }
}

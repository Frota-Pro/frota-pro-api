package br.com.frotasPro.api.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Chave de comparação de nomes de cidade/rota digitados à mão (o destino da
 * carga no WinThor é texto livre do faturista): ignora acento, caixa e espaços
 * repetidos, então "Poço Dantas" e "POCO  DANTAS" caem na mesma rota.
 *
 * Também ignora sufixos configuráveis no fim do nome (ParametroSistema.
 * sufixosRotaIgnorados, ex.: "FRIOS" — tipo da carga, não outra rota), então
 * "SOUSA FRIOS" e "SOUSA" também são a mesma rota. O sufixo só vale depois de
 * pelo menos uma palavra: um destino que é só "FRIOS" continua sendo "FRIOS".
 */
public final class NomeCidadeUtils {

    private NomeCidadeUtils() {
    }

    /** Lê a lista separada por vírgula (ou ponto e vírgula) já pronta pra comparação; vazio = nenhum sufixo. */
    public static List<String> sufixos(String configurado) {
        List<String> sufixos = new ArrayList<>();
        if (configurado == null) {
            return sufixos;
        }
        for (String item : configurado.split("[,;]")) {
            String normalizado = semAcentoMaiusculo(item);
            if (!normalizado.isEmpty() && !sufixos.contains(normalizado)) {
                sufixos.add(normalizado);
            }
        }
        return sufixos;
    }

    public static String chave(String nome, List<String> sufixos) {
        String semSufixo = nomeDaRota(nome, sufixos);
        return semSufixo == null ? null : semAcentoMaiusculo(semSufixo);
    }

    /** Nome para gravar na rota: o destino como veio (com acento), sem espaços repetidos e sem o sufixo ignorado. */
    public static String nomeDaRota(String destino, List<String> sufixos) {
        if (destino == null) {
            return null;
        }
        List<String> palavras = new ArrayList<>(Arrays.asList(destino.trim().split("\\s+")));
        for (String sufixo : sufixos) {
            int tamanho = sufixo.split(" ").length;
            if (palavras.size() > tamanho) {
                String ultimas = String.join(" ", palavras.subList(palavras.size() - tamanho, palavras.size()));
                if (semAcentoMaiusculo(ultimas).equals(sufixo)) {
                    palavras = new ArrayList<>(palavras.subList(0, palavras.size() - tamanho));
                    break;
                }
            }
        }
        return String.join(" ", palavras);
    }

    private static String semAcentoMaiusculo(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }
}

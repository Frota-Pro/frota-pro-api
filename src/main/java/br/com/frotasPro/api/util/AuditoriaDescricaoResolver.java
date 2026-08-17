package br.com.frotasPro.api.util;

import br.com.frotasPro.api.domain.enums.AcaoAuditoria;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduz "POST /carga" em algo que um humano lê na tela de auditoria, sem
 * precisar instrumentar cada controller/service na mão. Cobre os endpoints
 * de negócio mais relevantes com uma frase específica (ex: "Finalizou uma
 * carga") e cai num rótulo genérico ("Criou registro em X") pra qualquer
 * endpoint não mapeado — assim nenhuma ação passa despercebida da trilha,
 * mesmo que a descrição fique menos bonita.
 */
public class AuditoriaDescricaoResolver {

    /** Primeiro segmento do path -> nome de entidade em português, pra exibição. */
    private static final Map<String, String> LABEL_POR_SEGMENTO = new LinkedHashMap<>();

    static {
        LABEL_POR_SEGMENTO.put("abastecimento", "Abastecimento");
        LABEL_POR_SEGMENTO.put("ajudantes", "Ajudante");
        LABEL_POR_SEGMENTO.put("app-versao", "Versão do App");
        LABEL_POR_SEGMENTO.put("arquivos", "Arquivo");
        LABEL_POR_SEGMENTO.put("caminhao", "Caminhão");
        LABEL_POR_SEGMENTO.put("carga", "Carga");
        LABEL_POR_SEGMENTO.put("categorias-caminhao", "Categoria de Caminhão");
        LABEL_POR_SEGMENTO.put("cidades", "Cidade");
        LABEL_POR_SEGMENTO.put("configuracao-empresa", "Configuração da Empresa");
        LABEL_POR_SEGMENTO.put("conta", "Conta");
        LABEL_POR_SEGMENTO.put("despesas-parada", "Despesa de Parada");
        LABEL_POR_SEGMENTO.put("eixo", "Eixo");
        LABEL_POR_SEGMENTO.put("grupos-conta", "Grupo de Conta");
        LABEL_POR_SEGMENTO.put("manutencao", "Manutenção");
        LABEL_POR_SEGMENTO.put("mecanicos", "Mecânico");
        LABEL_POR_SEGMENTO.put("metas", "Meta");
        LABEL_POR_SEGMENTO.put("motorista", "Motorista");
        LABEL_POR_SEGMENTO.put("movimentacoes-sem-carga", "Movimentação sem Carga");
        LABEL_POR_SEGMENTO.put("multas", "Multa");
        LABEL_POR_SEGMENTO.put("notificacoes", "Notificação");
        LABEL_POR_SEGMENTO.put("oficinas", "Oficina");
        LABEL_POR_SEGMENTO.put("parada-carga", "Parada de Carga");
        LABEL_POR_SEGMENTO.put("parametro-sistema", "Parâmetros do Sistema");
        LABEL_POR_SEGMENTO.put("planos-manutencao-preventiva", "Plano de Manutenção Preventiva");
        LABEL_POR_SEGMENTO.put("pneus", "Pneu");
        LABEL_POR_SEGMENTO.put("postos-abastecimento", "Posto de Abastecimento");
        LABEL_POR_SEGMENTO.put("rota", "Rota");
        LABEL_POR_SEGMENTO.put("usuario", "Usuário");
    }

    /** path completo (sem query string) -> frase pronta, pras ações mais importantes do negócio. */
    private static final Map<String, String> FRASES_ESPECIAIS = new LinkedHashMap<>();

    static {
        FRASES_ESPECIAIS.put("/carga/iniciar", "Iniciou uma carga");
        FRASES_ESPECIAIS.put("/carga/finalizar", "Finalizou uma carga");
        FRASES_ESPECIAIS.put("/carga/verificar-winthor", "Verificou uma carga no WinThor");
    }

    /** sufixo do path -> frase pronta, quando o meio do path é um id/código variável. */
    private static final Map<String, String> FRASES_POR_SUFIXO = new LinkedHashMap<>();

    static {
        FRASES_POR_SUFIXO.put("/ordem-entrega", "Reordenou a rota de entrega de uma carga");
        FRASES_POR_SUFIXO.put("/observacao", "Alterou a observação de uma carga");
        FRASES_POR_SUFIXO.put("/transferir-notas", "Transferiu notas fiscais entre cargas");
        FRASES_POR_SUFIXO.put("/transferir-motorista", "Transferiu o motorista de uma carga");
        FRASES_POR_SUFIXO.put("/ativo", "Ativou ou desativou um usuário");
        FRASES_POR_SUFIXO.put("/senha", "Alterou a senha de um usuário");
        FRASES_POR_SUFIXO.put("/aprovar-orcamento", "Aprovou o orçamento de uma manutenção");
        FRASES_POR_SUFIXO.put("/enviar-email", "Reenviou uma nota fiscal por e-mail");
    }

    private AuditoriaDescricaoResolver() {
    }

    public static String resolverEntidade(String path) {
        String segmento = primeiroSegmento(path);
        return LABEL_POR_SEGMENTO.getOrDefault(segmento, humanizar(segmento));
    }

    public static String resolverDescricao(String metodoHttp, String path, AcaoAuditoria acao) {
        String frase = FRASES_ESPECIAIS.get(path);
        if (frase != null) {
            return frase;
        }

        for (Map.Entry<String, String> entry : FRASES_POR_SUFIXO.entrySet()) {
            if (path.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        String entidade = resolverEntidade(path);
        return switch (acao) {
            case CRIACAO -> "Criou registro em " + entidade;
            case ATUALIZACAO -> "Atualizou registro em " + entidade;
            case EXCLUSAO -> "Excluiu registro em " + entidade;
            default -> entidade;
        };
    }

    public static AcaoAuditoria resolverAcao(String metodoHttp) {
        return switch (metodoHttp.toUpperCase()) {
            case "POST" -> AcaoAuditoria.CRIACAO;
            case "PUT", "PATCH" -> AcaoAuditoria.ATUALIZACAO;
            case "DELETE" -> AcaoAuditoria.EXCLUSAO;
            default -> null;
        };
    }

    private static String primeiroSegmento(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        String semBarraInicial = path.startsWith("/") ? path.substring(1) : path;
        int barra = semBarraInicial.indexOf('/');
        return barra >= 0 ? semBarraInicial.substring(0, barra) : semBarraInicial;
    }

    private static String humanizar(String segmento) {
        if (segmento == null || segmento.isBlank()) {
            return "Sistema";
        }
        String[] partes = segmento.split("-");
        StringBuilder sb = new StringBuilder();
        for (String parte : partes) {
            if (parte.isBlank()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(parte.charAt(0))).append(parte.substring(1));
        }
        return !sb.isEmpty() ? sb.toString() : "Sistema";
    }
}

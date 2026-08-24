package br.com.frotasPro.api.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Traduz uma entidade JPA (a classe e uma instância) em algo legível pra
 * trilha de auditoria: o nome da "entidade" (ex: "Caminhão") e, se der,
 * um identificador de negócio (código/placa/número) pra colocar na
 * descrição, tipo "Atualizou Caminhão CAM-000123" em vez de só "Atualizou
 * Caminhão".
 */
public class AuditoriaEntidadeResolver {

    private static final Map<String, String> LABEL_POR_CLASSE = Map.ofEntries(
            Map.entry("Abastecimento", "Abastecimento"),
            Map.entry("Ajudante", "Ajudante"),
            Map.entry("AppVersao", "Versão do App"),
            Map.entry("Arquivo", "Arquivo"),
            Map.entry("Caminhao", "Caminhão"),
            Map.entry("Carga", "Carga"),
            Map.entry("CargaTransferencia", "Transferência de Carga"),
            Map.entry("CategoriaCaminhao", "Categoria de Caminhão"),
            Map.entry("Conta", "Conta"),
            Map.entry("ConfiguracaoEmpresa", "Configuração da Empresa"),
            Map.entry("DespesaParada", "Despesa de Parada"),
            Map.entry("DocumentoCaminhao", "Documento do Caminhão"),
            Map.entry("DocumentoManutencao", "Documento da Manutenção"),
            Map.entry("DocumentoMotorista", "Documento do Motorista"),
            Map.entry("Eixo", "Eixo"),
            Map.entry("GrupoConta", "Grupo de Conta"),
            Map.entry("Manutencao", "Manutenção"),
            Map.entry("ManutencaoItem", "Item de Manutenção"),
            Map.entry("Mecanico", "Mecânico"),
            Map.entry("Meta", "Meta"),
            Map.entry("MetaResultado", "Resultado de Meta"),
            Map.entry("Motorista", "Motorista"),
            Map.entry("MovimentacaoSemCarga", "Movimentação sem Carga"),
            Map.entry("Multa", "Multa"),
            Map.entry("MultaAnexo", "Anexo de Multa"),
            Map.entry("Notificacao", "Notificação"),
            Map.entry("Oficina", "Oficina"),
            Map.entry("ParadaCarga", "Parada de Carga"),
            Map.entry("ParametroSistema", "Parâmetros do Sistema"),
            Map.entry("PlanoManutencaoPreventiva", "Plano de Manutenção Preventiva"),
            Map.entry("Rota", "Rota"),
            Map.entry("RoteirizacaoCidade", "Roteirização de Cidade"),
            Map.entry("TrocaPneuManutencao", "Troca de Pneu"),
            Map.entry("Usuario", "Usuário")
    );

    private static final List<String> CAMPOS_IDENTIFICADORES = List.of(
            "codigo", "numeroCarga", "placa", "login", "nome"
    );

    private AuditoriaEntidadeResolver() {
    }

    public static String resolverLabel(Class<?> classe) {
        String nomeSimples = classe.getSimpleName();
        return LABEL_POR_CLASSE.getOrDefault(nomeSimples, humanizarPascalCase(nomeSimples));
    }

    public static String resolverIdentificador(Object entidade) {
        for (String nomeCampo : CAMPOS_IDENTIFICADORES) {
            String valor = lerCampoTexto(entidade, nomeCampo);
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return null;
    }

    private static String lerCampoTexto(Object entidade, String nomeCampo) {
        for (Class<?> clazz = entidade.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(nomeCampo);
                field.setAccessible(true);
                Object valor = field.get(entidade);
                return valor != null ? String.valueOf(valor) : null;
            } catch (NoSuchFieldException e) {
                // tenta a superclasse
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static String humanizarPascalCase(String nome) {
        if (nome == null || nome.isBlank()) {
            return "Sistema";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nome.length(); i++) {
            char c = nome.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                sb.append(' ');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}

package br.com.frotasPro.api.service.relatorios;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.ExporterInputItem;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleExporterInputItem;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JasperPdfService {

    public byte[] gerarPdfFromJasper(String jasperClasspath,
                                     Map<String, Object> params,
                                     List<?> linhas) {
        JasperPrint print = preencherRelatorio(jasperClasspath, params, linhas);
        renumerarPaginas(List.of(print));
        try {
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao exportar PDF Jasper: " + jasperClasspath, e);
        }
    }

    /** Preenche o relatório mas não exporta ainda — usado quando vários relatórios vão virar um PDF só (ver {@link #gerarPdfConsolidado}). */
    public JasperPrint preencherRelatorio(String jasperClasspath,
                                           Map<String, Object> params,
                                           List<?> linhas) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(jasperClasspath)) {
            if (in == null) {
                throw new RuntimeException("Arquivo .jasper nao encontrado no classpath: " + jasperClasspath);
            }
            JasperReport report = (JasperReport) JRLoader.loadObject(in);

            JRBeanCollectionDataSource ds =
                    new JRBeanCollectionDataSource(linhas == null ? Collections.emptyList() : linhas);

            return JasperFillManager.fillReport(report, params, ds);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao preencher relatorio Jasper: " + jasperClasspath, e);
        }
    }

    /**
     * Junta vários relatórios já preenchidos (um por {@link #preencherRelatorio}) num PDF
     * só, cada um começando numa página nova — é assim que dá pra gerar "todos os
     * motoristas de uma vez, um por página" sem mexer no .jrxml de cada relatório.
     */
    public byte[] gerarPdfConsolidado(List<JasperPrint> relatorios) {
        try {
            renumerarPaginas(relatorios);

            List<ExporterInputItem> itens = new ArrayList<>();
            for (JasperPrint relatorio : relatorios) {
                itens.add(new SimpleExporterInputItem(relatorio));
            }

            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(itens));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
            exporter.exportReport();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao consolidar PDFs Jasper", e);
        }
    }

    /** UUID do campo "Página X de Y" no rodapé de meta_mensal_motorista.jrxml. */
    private static final UUID UUID_RODAPE_PAGINA = UUID.fromString("00000000-0000-0000-0000-00000000021f");

    /**
     * Cada relatório é preenchido separadamente (um por motorista), então a
     * variável interna PAGE_NUMBER/PAGE_COUNT do Jasper vale só pra aquele
     * relatório sozinho — todo mundo mostra "Página 1 de 1" no rodapé, em
     * vez da posição real dentro do PDF consolidado. Renumera na marra,
     * sobrescrevendo o texto já renderizado do campo de rodapé (achado pelo
     * uuid dele no .jrxml) com a página global e o total real de páginas.
     */
    private void renumerarPaginas(List<JasperPrint> relatorios) {
        int totalPaginas = relatorios.stream().mapToInt(r -> r.getPages().size()).sum();
        int paginaAtual = 0;

        for (JasperPrint relatorio : relatorios) {
            for (JRPrintPage pagina : relatorio.getPages()) {
                paginaAtual++;

                for (JRPrintElement elemento : pagina.getElements()) {
                    if (elemento instanceof JRPrintText texto
                            && UUID_RODAPE_PAGINA.equals(elemento.getUUID())) {
                        texto.setText("Página " + paginaAtual + " de " + totalPaginas);
                    }
                }
            }
        }
    }
}

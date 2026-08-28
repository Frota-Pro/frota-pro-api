package br.com.frotasPro.api.service.relatorios;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class JasperPdfService {

    public byte[] gerarPdfFromJasper(String jasperClasspath,
                                     Map<String, Object> params,
                                     List<?> linhas) {
        JasperPrint print = preencherRelatorio(jasperClasspath, params, linhas);
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
}

package br.com.frotasPro.api.service.relatorios;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JasperPdfService {

    public byte[] gerarPdfFromJasper(String jasperClasspath,
                                     Map<String, Object> params,
                                     List<?> linhas) {
        try {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(jasperClasspath)) {
                if (in == null) {
                    throw new RuntimeException("Arquivo .jasper nao encontrado no classpath: " + jasperClasspath);
                }
                JasperReport report = (JasperReport) JRLoader.loadObject(in);

                JRBeanCollectionDataSource ds =
                        new JRBeanCollectionDataSource(linhas == null ? Collections.emptyList() : linhas);

                JasperPrint print = JasperFillManager.fillReport(report, params, ds);
                return JasperExportManager.exportReportToPdf(print);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF Jasper: " + jasperClasspath, e);
        }
    }
}

package br.com.frotasPro.api.service.relatorios;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JasperPdfService {

    public byte[] gerarPdfFromJrxml(String jrxmlClasspath,
                                    Map<String, Object> params,
                                    List<?> linhas) {
        try {
            ClassPathResource resource = new ClassPathResource(jrxmlClasspath);
            try (InputStream in = resource.getInputStream()) {
                JasperReport report = JasperCompileManager.compileReport(in);

                JRBeanCollectionDataSource ds =
                        new JRBeanCollectionDataSource(linhas == null ? Collections.emptyList() : linhas);

                JasperPrint print = JasperFillManager.fillReport(report, params, ds);
                return JasperExportManager.exportReportToPdf(print);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF Jasper: " + jrxmlClasspath, e);
        }
    }
}

package br.com.frotasPro.api.service.relatorios;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRSaver;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public final class JasperReportsBuildCompiler {

    private JasperReportsBuildCompiler() {
    }

    public static void main(String[] args) throws Exception {
        Path reportsDir = Paths.get("src", "main", "resources", "reports");
        Path outputDir = Paths.get("target", "classes", "reports");

        if (!Files.exists(reportsDir)) {
            throw new IllegalStateException("Diretorio de reports nao encontrado: " + reportsDir.toAbsolutePath());
        }

        Files.createDirectories(outputDir);

        try (Stream<Path> reportFiles = Files.list(reportsDir)) {
            reportFiles
                    .filter(path -> path.getFileName().toString().endsWith(".jrxml"))
                    .forEach(path -> compile(path, outputDir));
        }
    }

    private static void compile(Path jrxmlPath, Path outputDir) {
        String jrxmlName = jrxmlPath.getFileName().toString();
        String jasperName = jrxmlName.substring(0, jrxmlName.length() - ".jrxml".length()) + ".jasper";
        Path jasperPath = outputDir.resolve(jasperName);

        try (InputStream in = Files.newInputStream(jrxmlPath)) {
            JasperReport report = JasperCompileManager.compileReport(in);
            JRSaver.saveObject(report, jasperPath.toString());
            System.out.println("Compilado: " + jrxmlName + " -> " + jasperName);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao compilar report: " + jrxmlPath, e);
        }
    }
}

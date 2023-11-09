package com.contarbn;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperRunManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ReportPdfTest {

    private void createReport() throws Exception{

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("headerSubReportPath", this.getClass().getResource("/jasper_reports/test_1_header.jasper").toString());
        parameters.put("headerIntestazione", "TEST TEST TEST");

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream("/jasper_reports/test_1.jasper");

        byte[] report = JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());

        File outputFile = new File("C:\\temp\\report.pdf");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(report);
        }

    }

    public static void main(String[] args) throws Exception{
        ReportPdfTest reportPdfTest = new ReportPdfTest();
        reportPdfTest.createReport();
    }
}

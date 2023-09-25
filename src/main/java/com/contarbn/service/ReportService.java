package com.contarbn.service;

import com.contarbn.component.AsyncExecutor;
import com.contarbn.exception.GenericException;
import com.contarbn.model.Fattura;
import com.contarbn.util.Constants;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class ReportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportService.class);

    private final FatturaService fatturaService;
    private final StampaService stampaService;
    private final EmailService emailService;
    private final EmailPecService emailPecService;
    private final AsyncExecutor asyncExecutor;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    @Autowired
    public ReportService(final FatturaService fatturaService,
                         final StampaService stampaService,
                         final EmailService emailService,
                         final EmailPecService emailPecService,
                         final AsyncExecutor asyncExecutor
                      ){
        this.fatturaService = fatturaService;
        this.emailService = emailService;
        this.emailPecService = emailPecService;
        this.stampaService = stampaService;
        this.asyncExecutor = asyncExecutor;
    }

    private Set<Fattura> getFatture(Date dataDa, Date dataA, String numeroDa, String numeroA, String modalitaInvioFatture){
        Set<Fattura> fatture;

        Integer numeroFrom = null;
        Integer annoFrom = null;
        Integer numeroTo = null;
        Integer annoTo = null;

        if(!StringUtils.isEmpty(numeroDa)){
            numeroFrom = Integer.valueOf(StringUtils.substringBefore(numeroDa, "-"));
            annoFrom = Integer.valueOf(StringUtils.substringAfter(numeroDa, "-"));
        }
        if(!StringUtils.isEmpty(numeroA)){
            numeroTo = Integer.valueOf(StringUtils.substringBefore(numeroA, "-"));
            annoTo = Integer.valueOf(StringUtils.substringAfter(numeroA, "-"));
        }

        fatture = fatturaService.getFattureForReport(dataDa, dataA, numeroFrom, annoFrom, numeroTo, annoTo, modalitaInvioFatture);

        if(fatture.isEmpty()){
            throw new GenericException("Nessuna fattura disponibile per i filtri inseriti");
        }

        return fatture;
    }

    public Map<String, Object> createReportPdfFatture(Date dataDa, Date dataA, String numeroDa, String numeroA, String modalitaInvioFatture) throws Exception{

        Map<String, Object> result = new HashMap<>();

        LOGGER.info("Start creating report file for 'fatture'...");

        try{
            Set<Fattura> fatture = getFatture(dataDa, dataA, numeroDa, numeroA, modalitaInvioFatture);

            String pdfFileName = "Fatture_##.pdf";
            if(!StringUtils.isEmpty(modalitaInvioFatture) && modalitaInvioFatture.equalsIgnoreCase("cartaceo")){
                pdfFileName = "Fatture_cortesia_##.pdf";
            }
            byte[] pdfContent = null;

            String replacePlaceholder;
            if(dataDa != null && dataA != null){
                replacePlaceholder = sdf.format(dataDa) + "_" + sdf.format(dataA);
            } else {
                replacePlaceholder = numeroDa + "_" + numeroA;
            }
            pdfFileName = pdfFileName.replace("##", replacePlaceholder);

            if(!fatture.isEmpty()){

                List<CompletableFuture<FatturaFile>> completableFutures = new ArrayList<>();

                List<FatturaFile> fatturaFiles = new ArrayList<>();

                for(Fattura fattura : fatture){
                    completableFutures.add(asyncExecutor.executeAsyncCreateFatturaReport(stampaService, fattura));
                }
                CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));

                for(CompletableFuture<FatturaFile> completableFuture : completableFutures){
                    try{
                        FatturaFile fatturaFile = completableFuture.get();
                        fatturaFiles.add(fatturaFile);

                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }

                pdfContent = createPdfByteArray(fatturaFiles);
            }

            result.put("fileName", pdfFileName);
            result.put("content", pdfContent);

        } catch(Exception e){
            e.printStackTrace();
            throw e;
        }

        if(result.isEmpty()){
            throw new RuntimeException("Error creating report file for 'fatture'");
        }

        LOGGER.info("Successfully created report file for 'fatture'");

        return result;
    }

    public Map<String, Object> createReportPdfFattureCommercianti(Date dataDa, Date dataA, String numeroDa, String numeroA) throws Exception{

        Map<String, Object> result = new HashMap<>();

        LOGGER.info("Start creating report file for 'fatture per commercianti'...");

        try{
            Set<Fattura> fatture = getFatture(dataDa, dataA, numeroDa, numeroA, null);

            String pdfFileName = "Fatture_commercianti_##.pdf";
            byte[] pdfContent = null;

            String replacePlaceholder;
            String from;
            String to;
            if(dataDa != null && dataA != null){
                replacePlaceholder = sdf.format(dataDa) + "_" + sdf.format(dataA);
                sdf.applyPattern("dd/MM/YYYY");
                from = sdf.format(dataDa);
                to = sdf.format(dataA);
            } else {
                replacePlaceholder = numeroDa + "_" + numeroA;
                from = numeroDa.replace("-","/");
                to = numeroA.replace("-","/");
            }
            pdfFileName = pdfFileName.replace("##", replacePlaceholder);

            if(!fatture.isEmpty()){

                pdfContent = stampaService.generateFatturePerCommercianti(from, to , fatture);
            }

            result.put("fileName", pdfFileName);
            result.put("content", pdfContent);

        } catch(Exception e){
            e.printStackTrace();
            throw e;
        }

        if(result.isEmpty()){
            throw new RuntimeException("Error creating report file for 'fatture per commercianti'");
        }

        LOGGER.info("Successfully created report file for 'fatture per commercianti'");

        return result;
    }

    public String createReportEmailFatture(String action, Date dataDa, Date dataA, String numeroDa, String numeroA) throws Exception{

        LOGGER.info("Start creating report emails for fatture...");

        Set<Fattura> fatture;
        String txtFileName = "Invio_fatture_##.txt";
        boolean isPec = false;
        try {
            String modalitaInvioFatture = "mail";
            if (action.equals("spedizioneFatturePec")) {
                modalitaInvioFatture = "pec";
                isPec = true;
            }
            fatture = getFatture(dataDa, dataA, numeroDa, numeroA, modalitaInvioFatture);

            String replacePlaceholder;
            if (dataDa != null && dataA != null) {
                replacePlaceholder = sdf.format(dataDa) + "_" + sdf.format(dataA);
            } else {
                replacePlaceholder = numeroDa + "_" + numeroA;
            }
            txtFileName = txtFileName.replace("##", replacePlaceholder);

        } catch(GenericException ge){
            return ge.getMessage();
        } catch(Exception e){
            LOGGER.error("Error retrieving fatture", e);
            return "Errore nel recupero delle fatture";
        }

        asyncExecutor.executeAsyncSendFattureReport(emailService, emailPecService, stampaService, fatture, txtFileName, isPec);
        return "Invio email in corso. Riceverai un'email di riepilogo all'indirizzo "+Constants.DEFAULT_EMAIL;
    }

    public void checkRequestParams(Date dataDa, Date dataA, String numeroDa, String numeroA){
        if(dataDa == null && dataA == null && StringUtils.isEmpty(numeroDa) && StringUtils.isEmpty(numeroA)){
            throw new GenericException("Valorizzare un filtro di ricerca");
        }

        if((dataDa != null || dataA != null) && (!StringUtils.isEmpty(numeroDa) || !StringUtils.isEmpty(numeroA))){
            throw new GenericException("Non è possibile valorizzare sia il filtro di ricerca per date sia il filtro di ricerca per numero");
        }

        if((dataDa != null && dataA == null) || (dataDa == null && dataA != null)){
            throw new GenericException("Occorre valorizzare i filtri 'Data da' e 'Data a'");
        }

        if((!StringUtils.isEmpty(numeroDa) && StringUtils.isEmpty(numeroA)) || (StringUtils.isEmpty(numeroDa) && !StringUtils.isEmpty(numeroA))){
            throw new GenericException("Occorre valorizzare i filtri 'Numero da' e 'Numero a'");
        }

        if(dataDa != null && dataA != null && dataA.before(dataDa)){
            throw new GenericException("Il valore del filtro 'Data a' non può essere precedente al valore del filtro 'Data da'");
        }
    }

    public static HttpHeaders createHttpHeaders(String fileName){
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileName);
        headers.add(HttpHeaders.CACHE_CONTROL, Constants.HTTP_HEADER_CACHE_CONTROL_VALUE);
        headers.add(HttpHeaders.PRAGMA, Constants.HTTP_HEADER_PRAGMA_VALUE);
        headers.add(HttpHeaders.EXPIRES, Constants.HTTP_HEADER_EXPIRES_VALUE);
        return headers;
    }

    /*private byte[] createZipByteArray(List<FatturaFile> fatturaFiles) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        try {
            for (FatturaFile fatturaFileFile : fatturaFiles) {
                ZipEntry zipEntry = new ZipEntry(fatturaFileFile.fileName);
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(fatturaFileFile.content);
                zipOutputStream.closeEntry();
            }
        } finally {
            zipOutputStream.close();
        }
        return byteArrayOutputStream.toByteArray();
    }*/

    private byte[] createPdfByteArray(List<FatturaFile> fatturaFiles) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = null;
        PdfCopy writer = null;

        // sort FatturaFile list by fileName
        fatturaFiles.sort((f1, f2) -> f1.fileName.compareToIgnoreCase(f2.fileName));

        for(FatturaFile fatturaFile : fatturaFiles){
            try {
                PdfReader reader = new PdfReader(fatturaFile.content);
                int numberOfPages = reader.getNumberOfPages();

                if (document == null) {
                    document = new Document(reader.getPageSizeWithRotation(1));
                    writer = new PdfCopy(document, byteArrayOutputStream);
                    document.open();
                }
                PdfImportedPage page;
                for (int i = 0; i < numberOfPages;) {
                    ++i;
                    page = writer.getImportedPage(reader, i);
                    writer.addPage(page);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(document != null){
            document.close();
        }
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static class FatturaFile {
        public String fileName;
        public byte[] content;
    }

}

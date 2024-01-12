package com.contarbn.util;

import java.util.Arrays;
import java.util.List;

public interface Constants {

    List<Integer> ACTIVE_VALUES = Arrays.asList(0,1);

    List<Character> BARCODE_ALLOWED_CHARS = Arrays.asList('L', 'A', 'M', 'G', 'X');
    String BARCODE_REGEXP = "^.{start}(.{length})";

    String BARCODE_EAN13_TYPE = "EAN13";
    String BARCODE_EAN128_TYPE = "EAN128";

    String DEFAULT_ENCODING = "UTF-8";

    String DEFAULT_FORNITORE = "URBANI ELIA E MARTA";
    String DEFAULT_FORNITORE_INITIALS = "UR";
    String DEFAULT_EMAIL = "info@urbanialimentari.com";

    String DEFAULT_AUTISTA_COGNOME = "fumaroni";

    String DIVISA = "EUR";

    String EMAIL_INVIATA_OK = "Y";

    String FILE_SEPARATOR = "/";

    String HTTP_HEADER_PRAGMA_VALUE = "no-cache";
    String HTTP_HEADER_EXPIRES_VALUE = "0";
    String HTTP_HEADER_CACHE_CONTROL_VALUE = "no-cache, no-store, must-revalidate";

    String JASPER_PARAMETER_DDT_NOTA = "Assolve obblighi art.62,comm 1, del D.L. 24.01.12, n.1, convertito, con modificazioni, dalla legge 24.03.12, n.27. Contributo CONAI assolto dove dovuto";
    String JASPER_PARAMETER_DISPOSIZIONI_COMUNE_NOTA = "Verifica le disposizioni del tuo comune.";
    String JASPER_PARAMETER_FATTURA_NOTA = "Assolve obblighi art.62,comm 1, del D.L. 24.01.12, n.1, convertito, con modificazioni, dalla legge 24.03.12, n.27. Contributo CONAI assolto dove dovuto";
    String JASPER_PARAMETER_FATTURA_ACCOMPAGNATORIA_NOTA = "Assolve obblighi art.62,comm 1, del D.L. 24.01.12, n.1, convertito, con modificazioni, dalla legge 24.03.12, n.27. Contributo CONAI assolto dove dovuto";
    String JASPER_PARAMETER_RICEVUTA_PRIVATO_NOTA = "Assolve obblighi art.62,comm 1, del D.L. 24.01.12, n.1, convertito, con modificazioni, dalla legge 24.03.12, n.27. Contributo CONAI assolto dove dovuto";
    String JASPER_PARAMETER_SCHEDA_TECNICA_NOTA = "Il presente documento è di proprietà della ditta URBANI ALIMENTARI SNC e non può essere riprodotto o divulgato a terzi senza autorizzazione scritta dall'azienda produttrice. Il presente documento può subire modifiche o aggiornamenti, è pertanto compito degli utenti dello stesso, accertarsi di essere in possesso di una copia aggiornata.";
    String JASPER_REPORT_GIACENZE_INGREDIENTI = "/jasper_reports/giacenze_ingredienti.jasper";
    String JASPER_REPORT_DDTS = "/jasper_reports/ddts.jasper";
    String JASPER_REPORT_DDT = "/jasper_reports/ddt.jasper";
    String JASPER_REPORT_DOCUMENTI_ACQUISTO = "/jasper_reports/documenti_acquisto.jasper";
    String JASPER_REPORT_DDT_ACQUISTO = "/jasper_reports/ddt_acquisto.jasper";
    String JASPER_REPORT_ORDINI_AUTISTI = "/jasper_reports/ordini_autisti.jasper";
    String JASPER_REPORT_PAGAMENTI = "/jasper_reports/pagamenti.jasper";
    String JASPER_REPORT_NOTE_ACCREDITO = "/jasper_reports/note_accredito.jasper";
    String JASPER_REPORT_NOTA_ACCREDITO = "/jasper_reports/nota_accredito.jasper";
    String JASPER_REPORT_FATTURE = "/jasper_reports/fatture.jasper";
    String JASPER_REPORT_FATTURA = "/jasper_reports/fattura.jasper";
    String JASPER_REPORT_FATTURA_ACCOMPAGNATORIA = "/jasper_reports/fattura_accompagnatoria.jasper";
    String JASPER_REPORT_FATTURA_ACQUISTO = "/jasper_reports/fattura_acquisto.jasper";
    String JASPER_REPORT_FATTURA_ACCOMPAGNATORIA_ACQUISTO = "/jasper_reports/fattura_accompagnatoria_acquisto.jasper";
    String JASPER_REPORT_NOTA_RESO = "/jasper_reports/nota_reso.jasper";
    String JASPER_REPORT_RICEVUTE_PRIVATI = "/jasper_reports/ricevute_privati.jasper";
    String JASPER_REPORT_RICEVUTA_PRIVATO = "/jasper_reports/ricevuta_privato.jasper";
    String JASPER_REPORT_FATTURE_COMMERCIANTI = "/jasper_reports/fatture_commercianti.jasper";
    String JASPER_REPORT_ORDINE_FORNITORE = "/jasper_reports/ordine_fornitore.jasper";
    String JASPER_REPORT_LISTINO = "/jasper_reports/listino.jasper";
    String JASPER_REPORT_SCHEDA_TECNICA = "/jasper_reports/scheda_tecnica.jasper";
    String JASPER_REPORT_LOGO_IMAGE_PATH = "/jasper_reports/logo.png";
    String JASPER_REPORT_BOLLINO_IMAGE_PATH = "/jasper_reports/bollino.png";
    String JASPER_REPORT_HEADER_SUBREPORT_PATH = "/jasper_reports/header.jasper";

    String LABEL_TEMPLATE = "/label_generation/template.html";

    String MEDIA_TYPE_APPLICATION_PDF = "application/pdf";
    String MEDIA_TYPE_APPLICATION_XML = "application/xml";
    String MEDIA_TYPE_APPLICATION_ZIP = "application/zip";
    String MEDIA_TYPE_APPLICATION_TXT = "application/text";

    String NAZIONE = "IT";

    String ZONE_ID_EUROPE_ROME = "Europe/Rome";
}

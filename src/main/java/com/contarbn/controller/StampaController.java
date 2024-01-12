package com.contarbn.controller;

import com.contarbn.service.StampaService;
import com.contarbn.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path="/stampe")
//@SuppressWarnings("unused")
public class StampaController {

    // https://www.youtube.com/watch?v=fZtnoQpPzaw
    // https://www.qualogy.com/techblog/java-web/creating-report-with-list-containing-list-using-jasper-report

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final StampaService stampaService;


    @RequestMapping(method = GET, path = "/giacenze-ingredienti")
    @CrossOrigin
    public ResponseEntity<Resource> printGiacenzeIngredienti(@RequestParam(name = "ids") String ids) throws Exception{
        log.info("Creating pdf for 'giacenze-ingredienti' with ids {}", ids);

        // create report
        byte[] reportBytes = stampaService.generateGiacenzeIngredienti(ids);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'giacenze-ingredienti'");

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("giacenze-ingredienti.pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/pagamenti")
    @CrossOrigin
    public ResponseEntity<Resource> printPagamenti(@RequestParam(name = "ids") String ids) throws Exception{
        log.info("Creating pdf for 'pagamenti' with ids {}", ids);

        // create report
        byte[] reportBytes = stampaService.generatePagamenti(ids);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'pagamenti'");

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("pagamenti.pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/ddts")
    @CrossOrigin
    public ResponseEntity<Resource> printDdts(@RequestParam(name = "dataDa", required = false) Date dataDa,
                                              @RequestParam(name = "dataA", required = false) Date dataA,
                                              @RequestParam(name = "progressivo", required = false) Integer progressivo,
                                              @RequestParam(name = "importo", required = false) Float importo,
                                              @RequestParam(name = "tipoPagamento", required = false) Integer idTipoPagamento,
                                              @RequestParam(name = "cliente", required = false) String cliente,
                                              @RequestParam(name = "agente", required = false) Integer idAgente,
                                              @RequestParam(name = "autista", required = false) Integer idAutista,
                                              @RequestParam(name = "articolo", required = false) Integer idArticolo,
                                              @RequestParam(name = "stato", required = false) Integer idStato,
                                              @RequestParam(name = "pagato", required = false) Boolean pagato,
                                              @RequestParam(name = "idCliente", required = false) Integer idCliente,
                                              @RequestParam(name = "fatturato", required = false) Boolean fatturato) throws Exception{
        log.info("Creating pdf for list of 'ddt'");
        log.info("Request params: dataDa {}, dataA {}, progressivo {}, importo {}, tipoPagamento {}, cliente {}, agente {}, autista {}, articolo {}, stato {}, pagato {}, idCliente {}, fatturato {}",
                dataDa, dataA, progressivo, importo, idTipoPagamento, cliente, idAgente, idAutista, idArticolo, idStato, pagato, idCliente, fatturato);

        // create report
        byte[] reportBytes = stampaService.generateDdts(dataDa, dataA, progressivo, idCliente, cliente, idAgente, idAutista, idStato, pagato, fatturato, importo, idTipoPagamento, idArticolo);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'ddts'");

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("elenco_ddt.pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/ddts/{idDdt}")
    @CrossOrigin
    public ResponseEntity<Resource> printDdt(@PathVariable final Long idDdt) throws Exception{
        log.info("Creating pdf for 'ddt' with id '{}'", idDdt);

        // create report
        byte[] reportBytes = stampaService.generateDdt(idDdt);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'ddt' with id '{}'", idDdt);

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("ddt-"+idDdt+".pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = POST, path = "/ddts/selected", produces=Constants.MEDIA_TYPE_APPLICATION_ZIP)
    @CrossOrigin
    public ResponseEntity<Resource> printDdtsSelected(@RequestBody final Long[] ddts) throws Exception{
        log.info("Creating pdf merging pdfs of selected 'ddt'");

        String fileName = "ddts-"+ dateTimeFormatter.format(LocalDateTime.now())+".pdf";
        byte[] content;

        if(ddts == null || ddts.length == 0){
            throw new RuntimeException("No DDT to print");
        }

        content = stampaService.generateSingleDdt(ddts);

        log.info("Successfully create pdf for checked 'ddts'");

        ByteArrayResource resource = new ByteArrayResource(content);

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders(fileName))
                .contentLength(content.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/documenti-acquisto/distinta")
    @CrossOrigin
    public ResponseEntity<Resource> printDocumentiAcquistoDistinta(@RequestParam(name = "fornitore", required = false) String fornitore,
                                                                   @RequestParam(name = "numDocumento", required = false) String numDocumento,
                                                                   @RequestParam(name = "tipoDocumento", required = false) String tipoDocumento,
                                                                   @RequestParam(name = "dataDa", required = false) Date dataDa,
                                                                   @RequestParam(name = "dataA", required = false) Date dataA) throws Exception{
        log.info("Creating pdf for list of 'documenti acquisto'");
        log.info("Request params: fornitore{}, numDocumento {}, tipoDocumento{}, dataDa {}, dataA {}", fornitore, numDocumento, tipoDocumento, dataDa, dataA);

        // create report
        byte[] reportBytes = stampaService.generateDocumentiAcquistoDistinta(fornitore, numDocumento, tipoDocumento, dataDa, dataA);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for selected 'documenti-acquisto'");

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("documenti-acquisto-distinta.pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/ddts-acquisto/{idDdtAcquisto}")
    @CrossOrigin
    public ResponseEntity<Resource> printDdtAcquisto(@PathVariable final Long idDdtAcquisto) throws Exception{
        log.info("Creating pdf for 'ddt acquisto' with id '{}'", idDdtAcquisto);

        // create report
        byte[] reportBytes = stampaService.generateDdtAcquisto(idDdtAcquisto);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'ddt acquisto' with id '{}'", idDdtAcquisto);

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("ddt-acquisto"+idDdtAcquisto+".pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/ordini-autisti")
    @CrossOrigin
    public ResponseEntity<Resource> printOrdiniAutisti(
            @RequestParam(name = "idAutista") Long idAutista,
            @RequestParam(name = "dataConsegnaDa") Date dataConsegnaDa,
            @RequestParam(name = "dataConsegnaA") Date dataConsegnaA) throws Exception{
        log.info("Creating pdf for 'ordini-clienti' of 'autista' {}, 'dataConsegnaDa' {} and 'dataConsegnaA' {}", idAutista, dataConsegnaDa, dataConsegnaA);

        // create report
        byte[] reportBytes = stampaService.generateOrdiniAutisti(idAutista, dataConsegnaDa, dataConsegnaA);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'ordini-clienti' of 'autista' {}, 'dataConsegnaDa' {} and 'dataConsegnaA' {}", idAutista, dataConsegnaDa, dataConsegnaA);

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("ordini-clienti-autista.pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/note-accredito")
    @CrossOrigin
    public ResponseEntity<Resource> printNoteAccredito(@RequestParam(name = "dataDa", required = false) Date dataDa,
                                                       @RequestParam(name = "dataA", required = false) Date dataA,
                                                       @RequestParam(name = "progressivo", required = false) Integer progressivo,
                                                       @RequestParam(name = "importo", required = false) Float importo,
                                                       @RequestParam(name = "cliente", required = false) String cliente,
                                                       @RequestParam(name = "agente", required = false) Integer idAgente,
                                                       @RequestParam(name = "articolo", required = false) Integer idArticolo,
                                                       @RequestParam(name = "stato", required = false) Integer idStato) throws Exception{
        log.info("Creating pdf for list of 'note accredito'");

        // create report
        byte[] reportBytes = stampaService.generateNoteAccredito(dataDa, dataA, progressivo, importo, cliente, idAgente, idArticolo, idStato);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'note accredito'");

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("elenco_note_accredito.pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/note-accredito/{idNotaAccredito}")
    @CrossOrigin
    public ResponseEntity<Resource> printNotaAccredito(@PathVariable final Long idNotaAccredito) throws Exception{
        log.info("Creating pdf for 'nota accredito' with id '{}'", idNotaAccredito);

        // create report
        byte[] reportBytes = stampaService.generateNotaAccredito(idNotaAccredito);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'nota accredito' with id '{}'", idNotaAccredito);

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("nota-accredito-"+idNotaAccredito+".pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/fatture")
    @CrossOrigin
    public ResponseEntity<Resource> printFatture(@RequestParam(name = "dataDa", required = false) Date dataDa,
                                                 @RequestParam(name = "dataA", required = false) Date dataA,
                                                 @RequestParam(name = "progressivo", required = false) Integer progressivo,
                                                 @RequestParam(name = "importo", required = false) Float importo,
                                                 @RequestParam(name = "tipoPagamento", required = false) String idTipoPagamento,
                                                 @RequestParam(name = "cliente", required = false) String cliente,
                                                 @RequestParam(name = "agente", required = false) Integer idAgente,
                                                 @RequestParam(name = "articolo", required = false) Integer idArticolo,
                                                 @RequestParam(name = "stato", required = false) Integer idStato,
                                                 @RequestParam(name = "tipo", required = false) Integer idTipo) throws Exception{
        log.info("Creating pdf for list of 'fatture'");
        log.info("Request params: dataDa {}, dataA {}, progressivo {}, importo {}, tipoPagamento {}, cliente {}, agente {}, articolo {}, stato {}, tipo {}",
                dataDa, dataA, progressivo, importo, idTipoPagamento, cliente, idAgente, idArticolo, idStato, idTipo);

        // create report
        byte[] reportBytes = stampaService.generateFatture(dataDa, dataA, progressivo, importo, idTipoPagamento, cliente, idAgente, idArticolo, idStato, idTipo);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'fatture'");

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("elenco_fatture.pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/fatture/{idFattura}")
    @CrossOrigin
    public ResponseEntity<Resource> printFattura(@PathVariable final Long idFattura) throws Exception{
        log.info("Creating pdf for 'fattura' with id '{}'", idFattura);

        byte[] reportBytes = stampaService.generateFattura(idFattura);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'fattura' with id '{}'", idFattura);

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("fattura-"+idFattura+".pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/fatture-accompagnatorie/{idFatturaAccompagnatoria}")
    @CrossOrigin
    public ResponseEntity<Resource> printFatturaAccompagnatoria(@PathVariable final Long idFatturaAccompagnatoria) throws Exception{
        log.info("Creating pdf for 'fattura accompagnatoria' with id '{}'", idFatturaAccompagnatoria);

        byte[] reportBytes = stampaService.generateFatturaAccompagnatoria(idFatturaAccompagnatoria);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'fattura accompagnatoria' with id '{}'", idFatturaAccompagnatoria);

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("fattura-accompagnatoria-"+idFatturaAccompagnatoria+".pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/fatture-acquisto/{idFatturaAcquisto}")
    @CrossOrigin
    public ResponseEntity<Resource> printFatturaAcquisto(@PathVariable final Long idFatturaAcquisto) throws Exception{
        log.info("Creating pdf for 'fattura acquisto' with id '{}'", idFatturaAcquisto);

        byte[] reportBytes = stampaService.generateFatturaAcquisto(idFatturaAcquisto);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'fattura acquisto' with id '{}'", idFatturaAcquisto);

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("fattura-acquisto"+idFatturaAcquisto+".pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/fatture-accompagnatorie-acquisto/{idFatturaAccompagnatoriaAcquisto}")
    @CrossOrigin
    public ResponseEntity<Resource> printFatturaAccompagnatoriaAcquisto(@PathVariable final Long idFatturaAccompagnatoriaAcquisto) throws Exception{
        log.info("Creating pdf for 'fattura accompagnatoria acquisto' with id '{}'", idFatturaAccompagnatoriaAcquisto);

        byte[] reportBytes = stampaService.generateFatturaAccompagnatoriaAcquisto(idFatturaAccompagnatoriaAcquisto);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'fattura accompagnatoria acquisto' with id '{}'", idFatturaAccompagnatoriaAcquisto);

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("fattura-accompagnatoria-acquisto-"+idFatturaAccompagnatoriaAcquisto+".pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/note-reso/{idNotaReso}")
    @CrossOrigin
    public ResponseEntity<Resource> printNotaReso(@PathVariable final Long idNotaReso) throws Exception{
        log.info("Creating pdf for 'nota reso' with id '{}'", idNotaReso);

        // create report
        byte[] reportBytes = stampaService.generateNotaReso(idNotaReso);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'nota reso' with id '{}'", idNotaReso);

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("nota-reso-"+idNotaReso+".pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/ricevute-privati")
    @CrossOrigin
    public ResponseEntity<Resource> printRicevutePrivati(@RequestParam(name = "ids") String ids) throws Exception{
        log.info("Creating pdf for 'ricevute privati' with ids {}", ids);

        // create report
        byte[] reportBytes = stampaService.generateRicevutePrivati(ids);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'ricevute privati'");

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("elenco_ricevute_privati.pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/ricevute-privati/{idRicevutaPrivato}")
    @CrossOrigin
    public ResponseEntity<Resource> printRicevutaPrivato(@PathVariable final Long idRicevutaPrivato) throws Exception{
        log.info("Creating pdf for 'ricevuta privato' with id '{}'", idRicevutaPrivato);

        // create report
        byte[] reportBytes = stampaService.generateRicevutaPrivato(idRicevutaPrivato);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'ricevuta privato' with id '{}'", idRicevutaPrivato);

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("ricevuta-privato-"+idRicevutaPrivato+".pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/ordini-fornitori/{idOrdineFornitore}")
    @CrossOrigin
    public ResponseEntity<Resource> printOrdineFornitore(@PathVariable final Long idOrdineFornitore) throws Exception{
        log.info("Creating pdf for 'ordine-fornitore' with id '{}'", idOrdineFornitore);

        // create report
        byte[] reportBytes = stampaService.generateOrdineFornitore(idOrdineFornitore);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'ordine-fornitore' with id '{}'", idOrdineFornitore);

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("ordine-fornitore-"+idOrdineFornitore+".pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/listini/{idListino}")
    @CrossOrigin
    public ResponseEntity<Resource> printListino(@PathVariable final Long idListino,
                                                 @RequestParam(name = "fornitore", required = false) Long idFornitore,
                                                 @RequestParam(name = "categoriaArticolo", required = false) Long idCategoriaArticolo,
                                                 @RequestParam(name = "orderBy", required = false) String orderBy) throws Exception{
        log.info("Creating pdf for 'listino' with id '{}', filtered by fornitore '{}' and categoria articolo '{}', order by '{}'", idListino, idFornitore, idCategoriaArticolo, orderBy);

        // create report
        byte[] reportBytes = stampaService.generateListino(idListino, idFornitore, idCategoriaArticolo, orderBy);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'listino' with id '{}' order by '{}'", idListino, orderBy);

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("listino-"+idListino+".pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }

    @RequestMapping(method = GET, path = "/schede-tecniche/{idSchedaTecnica}")
    @CrossOrigin
    public ResponseEntity<Resource> printSchedaTecnica(@PathVariable final Long idSchedaTecnica) throws Exception{
        log.info("Creating pdf for 'scheda-tecnica' with id '{}'", idSchedaTecnica);

        // create report
        byte[] reportBytes = stampaService.generateSchedaTecnica(idSchedaTecnica);

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'scheda-tecnica' with id '{}'", idSchedaTecnica);

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("scheda-tecnica-"+idSchedaTecnica+".pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }
}
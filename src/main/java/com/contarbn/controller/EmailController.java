package com.contarbn.controller;

import com.contarbn.model.*;
import com.contarbn.service.EmailService;
import com.contarbn.service.StampaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SuppressWarnings({"unused"})
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path="/emails")
public class EmailController {

    private final StampaService stampaService;

    private final EmailService emailService;

    @RequestMapping(method = GET, path = "/ddts/{idDdt}")
    @CrossOrigin
    public void sendEmailDdt(@PathVariable final Long idDdt) throws Exception{

        log.info("Sending email for ddt '{}'", idDdt);

        byte[] reportBytes = stampaService.generateDdt(idDdt);
        Ddt ddt = stampaService.getDdt(idDdt);
        emailService.sendEmailDdt(ddt, reportBytes);

        log.info("Successfully sent email for ddt '{}'", idDdt);
    }

    @RequestMapping(method = GET, path = "/fatture/{idFattura}")
    @CrossOrigin
    public void sendEmailFattura(@PathVariable final Long idFattura) throws Exception{

        log.info("Sending email for fattura '{}'", idFattura);

        byte[] reportBytes = stampaService.generateFattura(idFattura);
        Fattura fattura = stampaService.getFattura(idFattura);
        emailService.sendEmailFattura(fattura, reportBytes);

        log.info("Successfully sent email for fattura '{}'", idFattura);
    }

    @RequestMapping(method = GET, path = "/note-accredito/{idNotaAccredito}")
    @CrossOrigin
    public void sendEmailNotaAccredito(@PathVariable final Long idNotaAccredito) throws Exception{

        log.info("Sending email for notaAccredito '{}'", idNotaAccredito);

        byte[] reportBytes = stampaService.generateNotaAccredito(idNotaAccredito);
        NotaAccredito notaAccredito = stampaService.getNotaAccredito(idNotaAccredito);
        emailService.sendEmailNotaAccredito(notaAccredito, reportBytes);

        log.info("Successfully sent email for notaAccredito '{}'", idNotaAccredito);
    }

    @RequestMapping(method = GET, path = "/ordini-fornitori/{idOrdineFornitore}")
    @CrossOrigin
    public void sendEmailOrdineFornitore(@PathVariable final Long idOrdineFornitore) throws Exception{

        log.info("Sending email for ordine-fornitore '{}'", idOrdineFornitore);

        byte[] reportBytes = stampaService.generateOrdineFornitore(idOrdineFornitore);
        OrdineFornitore ordineFornitore = stampaService.getOrdineFornitore(idOrdineFornitore);
        emailService.sendEmailOrdineFornitore(ordineFornitore, reportBytes);

        log.info("Successfully sent email for ordine-fornitore '{}'", idOrdineFornitore);
    }

    @RequestMapping(method = POST, path = "/schede-tecniche/{idSchedaTecnica}")
    @CrossOrigin
    public void sendEmailOrdineFornitoreSchedaTecnica(@PathVariable final Long idSchedaTecnica,
                                                      @RequestBody final Map<String, Object> bodyRequest) throws Exception {
        log.info("Sending email for scheda-tecnica '{}'", idSchedaTecnica);
        SchedaTecnica schedaTecnica = stampaService.getSchedaTecnica(idSchedaTecnica);
        emailService.sendEmailSchedaTecnica(schedaTecnica, bodyRequest);
    }

}
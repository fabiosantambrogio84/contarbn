package com.contarbn.controller;

import com.contarbn.service.CleanupService;
import com.contarbn.service.GiacenzaArticoloService;
import com.contarbn.util.enumeration.OperationAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path="/configurazione/operations")
public class OperationsController {

    private final CleanupService cleanupService;
    private final GiacenzaArticoloService giacenzaArticoloService;

    @RequestMapping(method = GET)
    @CrossOrigin
    public List<Map<String, String>> getActions() {
        log.info("Performing GET request for retrieving list of 'operations actions'");
        return OperationAction.getActions();
    }

    @RequestMapping(method = DELETE, path = "/cleanup/ordini-clienti/evasi-expired")
    @CrossOrigin
    public void deleteEvasiAndExpiredOrdiniClienti(@RequestParam(name = "days", required = false) Integer days) {
        log.info("Performing DELETE request for deleting expired and evasi Ordini Clienti");
        cleanupService.deleteEvasiAndExpiredOrdiniClienti(days);
    }

    @RequestMapping(method = DELETE, path = "/cleanup/etichette")
    @CrossOrigin
    public void deleteEtichette(@RequestParam(name = "days", required = false) Integer days) {
        log.info("Performing DELETE request for deleting Etichette");
        cleanupService.deleteEtichette(days);
    }

    @RequestMapping(method = POST, path = "/compute/giacenze-articoli")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void computeGiacenzeArticoli(@RequestParam(name = "idArticoloFrom") Integer idArticoloFrom,
                                        @RequestParam(name = "idArticoloTo") Integer idArticoloTo) {
        log.info("Performing GET request for computing 'giacenze articolo'");
        giacenzaArticoloService.computeGiacenzaBulk(idArticoloFrom, idArticoloTo);
    }

}
package com.contarbn.controller;

import com.contarbn.model.*;
import com.contarbn.service.OrdineClienteStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path="/ordini-clienti-statistiche")
public class OrdineClienteStatsController {

    private static Logger LOGGER = LoggerFactory.getLogger(OrdineClienteStatsController.class);

    private final OrdineClienteStatsService ordineClienteStatsService;

    @Autowired
    public OrdineClienteStatsController(final OrdineClienteStatsService ordineClienteStatsService){
        this.ordineClienteStatsService = ordineClienteStatsService;
    }

    @RequestMapping(method = GET, path = "/settimana")
    @CrossOrigin
    public List<Articolo> getStatsWeek(@RequestParam(name = "idCliente") Integer idCliente,
                                       @RequestParam(name = "idPuntoConsegna") Integer idPuntoConsegna) {
        LOGGER.info("Performing GET request for retrieving list of 'articoli' for 'ordini clienti statistiche settimana' cliente '{}' and puntoConsegna '{}'", idCliente, idPuntoConsegna);
        return ordineClienteStatsService.getArticoliOrdiniClientiStatsWeekByIdClienteAndIdPuntoConsegna(idCliente.longValue(), idPuntoConsegna.longValue());
    }

    @RequestMapping(method = GET, path = "/mese")
    @CrossOrigin
    public List<Articolo> getStatsMonth(@RequestParam(name = "idCliente") Integer idCliente,
                                       @RequestParam(name = "idPuntoConsegna") Integer idPuntoConsegna) {
        LOGGER.info("Performing GET request for retrieving list of 'articoli' for 'ordini clienti statistiche mese' cliente '{}' and puntoConsegna '{}'", idCliente, idPuntoConsegna);
        return ordineClienteStatsService.getArticoliOrdiniClientiStatsMonthByIdClienteAndIdPuntoConsegna(idCliente.longValue(), idPuntoConsegna.longValue());
    }
}

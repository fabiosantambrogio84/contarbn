package com.contarbn.controller;

import com.contarbn.model.GiacenzaArticolo;
import com.contarbn.model.beans.PageResponse;
import com.contarbn.model.views.VGiacenzaArticolo;
import com.contarbn.service.GiacenzaArticoloService;
import com.contarbn.util.ResponseUtils;
import com.contarbn.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path="/giacenze-articoli")
public class GiacenzaArticoloController {

    private final GiacenzaArticoloService giacenzaArticoloService;

    @RequestMapping(method = GET, path = "/search")
    @CrossOrigin
    public PageResponse search(@RequestParam(name = "draw", required = false) Integer draw,
                               @RequestParam(name = "start", required = false) Integer start,
                               @RequestParam(name = "length", required = false) Integer length,
                               @RequestParam(name = "articolo", required = false) String articolo,
                               @RequestParam(name = "attivo", required = false) Boolean attivo,
                               @RequestParam(name = "idFornitore", required = false) Integer idFornitore,
                               @RequestParam(name = "lotto", required = false) String lotto,
                               @RequestParam(name = "scadenza", required = false) Date scadenza,
                               @RequestParam(name = "scaduto", required = false) Boolean scaduto,
                               @RequestParam Map<String,String> allRequestParams) {
        log.info("Performing GET request for retrieving list of 'giacenze articoli' (with quantita != 0)");
        log.info("Request params: draw {}, start {}, length {}, articolo {}, attivo {}, idFornitore {}, lotto {}, scadenza {}, scaduto {}",
                draw, start, length, articolo, attivo, idFornitore, lotto, scadenza, scaduto);

        List<VGiacenzaArticolo> data = giacenzaArticoloService.getAllByFilters(draw, start, length, Utils.getSortOrders(allRequestParams), articolo, attivo, idFornitore, lotto, scadenza, scaduto);
        Integer recordsCount = giacenzaArticoloService.getCountByFilters(articolo, attivo, idFornitore, lotto, scadenza, scaduto);

        return ResponseUtils.createPageResponse(draw, recordsCount, recordsCount, data);
    }

    @RequestMapping(method = GET, path = "/{idArticolo}")
    @CrossOrigin
    public Map<String, Object> getOne(@PathVariable final Long idArticolo) {
        log.info("Performing GET request for retrieving 'giacenza articolo' of articolo '{}'", idArticolo);
        return giacenzaArticoloService.getOne(idArticolo);
    }

    @RequestMapping(method = GET, path = "/{idArticolo}/list")
    @CrossOrigin
    public List<GiacenzaArticolo> getAllByArticolo(@PathVariable final Long idArticolo) {
        log.info("Performing GET request for retrieving 'giacenza articolo' of articolo '{}'", idArticolo);
        return giacenzaArticoloService.getNotAggregateByArticolo(idArticolo);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public GiacenzaArticolo create(@RequestBody final GiacenzaArticolo giacenzaArticolo){
        log.info("Performing POST request for creating 'giacenza articolo'");
        return giacenzaArticoloService.create(giacenzaArticolo);
    }

    @RequestMapping(method = PUT)
    @CrossOrigin
    public List<GiacenzaArticolo> updateBulk(@RequestBody final List<GiacenzaArticolo> giacenzeArticolo){
        log.info("Performing PUT request for bulk updating 'giacenza articolo'");
        return giacenzaArticoloService.updateBulk(giacenzeArticolo);
    }

    @RequestMapping(method = POST, path = "/operations/delete")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void bulkDelete(@RequestBody final List<Long> idArticoli){
        log.info("Performing BULK DELETE operation on 'giacenze articoli' by 'idArticolo' (number of elements to delete: {})", idArticoli.size());
        giacenzaArticoloService.bulkDelete(idArticoli);
    }
}
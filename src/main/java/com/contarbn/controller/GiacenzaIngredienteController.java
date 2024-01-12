package com.contarbn.controller;

import com.contarbn.model.GiacenzaIngrediente;
import com.contarbn.model.beans.PageResponse;
import com.contarbn.model.views.VGiacenzaIngrediente;
import com.contarbn.service.GiacenzaIngredienteService;
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
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path="/giacenze-ingredienti")
public class GiacenzaIngredienteController {

    private final GiacenzaIngredienteService giacenzaIngredienteService;

    @RequestMapping(method = GET, path = "/search")
    @CrossOrigin
    public PageResponse search(@RequestParam(name = "draw", required = false) Integer draw,
                               @RequestParam(name = "start", required = false) Integer start,
                               @RequestParam(name = "length", required = false) Integer length,
                               @RequestParam(name = "ingrediente", required = false) String ingrediente,
                               @RequestParam(name = "attivo", required = false) Boolean attivo,
                               @RequestParam(name = "idFornitore", required = false) Integer idFornitore,
                               @RequestParam(name = "lotto", required = false) String lotto,
                               @RequestParam(name = "scadenza", required = false) Date scadenza,
                               @RequestParam(name = "scaduto", required = false) Boolean scaduto,
                               @RequestParam Map<String,String> allRequestParams) {
        log.info("Performing GET request for retrieving list of 'giacenze ingredienti' (with quantita != 0)");
        log.info("Request params: draw {}, start {}, length {}, ingrediente {}, attivo {}, idFornitore {}, lotto {}, scadenza {}, scaduto {}",
                draw, start, length, ingrediente, attivo, idFornitore, lotto, scadenza, scaduto);

        List<VGiacenzaIngrediente> data = giacenzaIngredienteService.getAllByFilters(draw, start, length, Utils.getSortOrders(allRequestParams), ingrediente, attivo, idFornitore, lotto, scadenza, scaduto);
        Integer recordsCount = giacenzaIngredienteService.getCountByFilters(ingrediente, attivo, idFornitore, lotto, scadenza, scaduto);

        return ResponseUtils.createPageResponse(draw, recordsCount, recordsCount, data);
    }

    @RequestMapping(method = GET, path = "/{idIngrediente}")
    @CrossOrigin
    public Map<String, Object> getOne(@PathVariable final Long idIngrediente) {
        log.info("Performing GET request for retrieving 'giacenza ingrediente' of ingrediente '{}'", idIngrediente);
        return giacenzaIngredienteService.getOne(idIngrediente);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public GiacenzaIngrediente create(@RequestBody final GiacenzaIngrediente giacenzaIngrediente){
        log.info("Performing POST request for creating 'giacenza ingrediente'");
        return giacenzaIngredienteService.create(giacenzaIngrediente);
    }

    @RequestMapping(method = POST, path = "/operations/delete")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void bulkDelete(@RequestBody final List<Long> idIngredienti){
        log.info("Performing BULK DELETE operation on 'giacenze ingrediente' by 'idIngrediente' (number of elements to delete: {})", idIngredienti.size());
        giacenzaIngredienteService.bulkDelete(idIngredienti);
    }
}
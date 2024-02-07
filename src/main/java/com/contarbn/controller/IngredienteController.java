package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.Ingrediente;
import com.contarbn.model.beans.PageResponse;
import com.contarbn.model.views.VIngrediente;
import com.contarbn.service.IngredienteService;
import com.contarbn.util.ResponseUtils;
import com.contarbn.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping(path="/ingredienti")
public class IngredienteController {

    private final IngredienteService ingredienteService;

    @RequestMapping(method = GET, path = "/search")
    public PageResponse search(@RequestParam(name = "draw", required = false) Integer draw,
                               @RequestParam(name = "start", required = false) Integer start,
                               @RequestParam(name = "length", required = false) Integer length,
                               @RequestParam(name = "codice", required = false) String codice,
                               @RequestParam(name = "descrizione", required = false) String descrizione,
                               @RequestParam(name = "idFornitore", required = false) Integer idFornitore,
                               @RequestParam(name = "composto", required = false) Boolean composto,
                               @RequestParam(name = "attivo", required = false) Boolean attivo,
                               @RequestParam Map<String,String> allRequestParams) {
        log.info("Performing GET request for retrieving list of 'ingredienti'");
        log.info("Request params: draw {}, start {}, length {}, codice {}, descrizione {}, idFornitore {}, composto {}, attivo {}", draw, start, length, codice, descrizione, idFornitore, composto, attivo);

        List<VIngrediente> data = ingredienteService.getAllByFilters(draw, start, length, Utils.getSortOrders(allRequestParams), codice, descrizione, idFornitore, composto, attivo);
        Integer recordsCount = ingredienteService.getCountByFilters(codice, descrizione, idFornitore, composto, attivo);

        return ResponseUtils.createPageResponse(draw, recordsCount, recordsCount, data);
    }

    @RequestMapping(method = GET, path = "/{ingredienteId}")
    public Ingrediente getOne(@PathVariable final Long ingredienteId) {
        log.info("Performing GET request for retrieving 'ingrediente' '{}'", ingredienteId);
        return ingredienteService.getOne(ingredienteId);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    public Ingrediente create(@RequestBody final Ingrediente ingrediente){
        log.info("Performing POST request for creating 'ingrediente'");
        return ingredienteService.create(ingrediente);
    }

    @RequestMapping(method = PUT, path = "/{ingredienteId}")
    public Ingrediente update(@PathVariable final Long ingredienteId, @RequestBody final Ingrediente ingrediente){
        log.info("Performing PUT request for updating 'ingrediente' '{}'", ingredienteId);
        if (!Objects.equals(ingredienteId, ingrediente.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return ingredienteService.update(ingrediente);
    }

    @RequestMapping(method = DELETE, path = "/{ingredienteId}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@PathVariable final Long ingredienteId){
        log.info("Performing DELETE request for deleting 'ingrediente' '{}'", ingredienteId);
        ingredienteService.delete(ingredienteId);
    }

    @RequestMapping(method = POST, path = "/operations/delete")
    @ResponseStatus(NO_CONTENT)
    public void deleteBulk(@RequestBody final List<Long> idIngredienti){
        log.info("Performing BULK DELETE operation on 'ingredienti' by 'idIngrediente' (number of elements to delete: {})", idIngredienti.size());
        ingredienteService.deleteBulk(idIngredienti);
    }
}

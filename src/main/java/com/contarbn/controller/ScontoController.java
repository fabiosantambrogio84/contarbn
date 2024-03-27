package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.Sconto;
import com.contarbn.service.ScontoService;
import com.contarbn.util.enumeration.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path="/sconti")
public class ScontoController {

    private final ScontoService scontoService;

    @RequestMapping(method = GET)
    @CrossOrigin
    public List<Sconto> getAll(@RequestParam(required = false) String tipologia,
                               @RequestParam(required = false) Integer idCliente,
                               @RequestParam(required = false) Integer idFornitore,
                               @RequestParam(required = false) Date data) {

        if(!StringUtils.isEmpty(tipologia) && idCliente == null && data == null){
            log.info("Performing GET request for retrieving list of 'sconti' filtered by tipologia '{}'", tipologia);
            return scontoService.getAllByTipologia(tipologia);
        } else if(idCliente != null && data != null){
            log.info("Performing GET request for retrieving list of 'sconti' filtered by cliente '{}' and date '{}'", idCliente, data);
            return scontoService.getValidSconti(Resource.CLIENTE, Long.valueOf(idCliente), data);
        } else if(idFornitore != null && data != null) {
            log.info("Performing GET request for retrieving list of 'sconti' filtered by fornitore '{}' and date '{}'", idFornitore, data);
            return scontoService.getValidSconti(Resource.FORNITORE, Long.valueOf(idFornitore), data);
        } else {
            log.info("Performing GET request for retrieving list of 'sconti'");
            return scontoService.getAll();
        }
    }

    @RequestMapping(method = GET, path = "/{scontoId}")
    @CrossOrigin
    public Sconto getOne(@PathVariable final Long scontoId) {
        log.info("Performing GET request for retrieving 'sconto' '{}'", scontoId);
        return scontoService.getOne(scontoId);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public List<Sconto> create(@RequestBody final List<Sconto> sconti){
        log.info("Performing POST request for creating 'sconto'");
        return scontoService.create(sconti);
    }

    @RequestMapping(method = PUT, path = "/{scontoId}")
    @CrossOrigin
    public Sconto update(@PathVariable final Long scontoId, @RequestBody final Sconto sconto){
        log.info("Performing PUT request for updating 'sconto' '{}'", scontoId);
        if (!Objects.equals(scontoId, sconto.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return scontoService.update(sconto);
    }

    @RequestMapping(method = DELETE, path = "/{scontoId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long scontoId){
        log.info("Performing DELETE request for deleting 'sconto' '{}'", scontoId);
        scontoService.delete(scontoId);
    }
}

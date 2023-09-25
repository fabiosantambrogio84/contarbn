package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.FatturaAccompagnatoria;
import com.contarbn.service.FatturaAccompagnatoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path="/fatture-accompagnatorie")
public class FatturaAccompagnatoriaController {

    private static Logger LOGGER = LoggerFactory.getLogger(FatturaAccompagnatoriaController.class);

    private final FatturaAccompagnatoriaService fatturaAccompagnatoriaService;

    @Autowired
    public FatturaAccompagnatoriaController(final FatturaAccompagnatoriaService fatturaAccompagnatoriaService){
        this.fatturaAccompagnatoriaService = fatturaAccompagnatoriaService;
    }

    @RequestMapping(method = GET, path = "/{fatturaAccompagnatoriaId}")
    @CrossOrigin
    public FatturaAccompagnatoria getOne(@PathVariable final Long fatturaAccompagnatoriaId) {
        LOGGER.info("Performing GET request for retrieving 'fattura accompagnatoria' '{}'", fatturaAccompagnatoriaId);
        return fatturaAccompagnatoriaService.getOne(fatturaAccompagnatoriaId);
    }

    @RequestMapping(method = GET, path = "/progressivo")
    @CrossOrigin
    public Map<String, Integer> getAnnoAndProgressivo() {
        LOGGER.info("Performing GET request for retrieving 'anno' and 'progressivo' for a new fattura accompagnatoria");
        return fatturaAccompagnatoriaService.getAnnoAndProgressivo();
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public FatturaAccompagnatoria create(@RequestBody final FatturaAccompagnatoria fatturaAccompagnatoria){
        LOGGER.info("Performing POST request for creating 'fattura accompagnatoria'");
        return fatturaAccompagnatoriaService.create(fatturaAccompagnatoria);
    }

    @RequestMapping(method = PATCH, path = "/{fatturaAccompagnatoriaId}")
    @CrossOrigin
    public FatturaAccompagnatoria patch(@PathVariable final Long fatturaAccompagnatoriaId, @RequestBody final Map<String,Object> patchFatturaAccompagnatoria){
        LOGGER.info("Performing PATCH request for updating 'fattura accompagnatoria' '{}'", fatturaAccompagnatoriaId);
        Long id = Long.valueOf((Integer)patchFatturaAccompagnatoria.get("id"));
        if (!Objects.equals(fatturaAccompagnatoriaId, id)) {
            throw new CannotChangeResourceIdException();
        }
        return fatturaAccompagnatoriaService.patch(patchFatturaAccompagnatoria);
    }

    @RequestMapping(method = DELETE, path = "/{fatturaAccompagnatoriaId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long fatturaAccompagnatoriaId){
        LOGGER.info("Performing DELETE request for deleting 'fattura accompagnatoria' '{}'", fatturaAccompagnatoriaId);
        fatturaAccompagnatoriaService.delete(fatturaAccompagnatoriaId);
    }

}

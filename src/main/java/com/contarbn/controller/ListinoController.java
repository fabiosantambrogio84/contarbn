package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Listino;
import com.contarbn.model.ListinoPrezzo;
import com.contarbn.model.ListinoPrezzoVariazione;
import com.contarbn.service.ListinoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path="/listini")
public class ListinoController {

    private final static Logger LOGGER = LoggerFactory.getLogger(ListinoController.class);

    private final ListinoService listinoService;

    @Autowired
    public ListinoController(final ListinoService listinoService){
        this.listinoService = listinoService;
    }

    @RequestMapping(method = GET)
    @CrossOrigin
    public Set<Listino> getAll(@RequestParam(name = "search[value]", required = false) String searchValue) throws Exception{
        LOGGER.info("Performing GET request for retrieving list of 'listini'");
        //LOGGER.info("Query parameter 'search[value]' equal to '{}'", searchValue);
        return listinoService.getAll();
    }

    @RequestMapping(method = GET, path = "/{listinoId}")
    @CrossOrigin
    public Listino getOne(@PathVariable final Long listinoId) {
        LOGGER.info("Performing GET request for retrieving 'listino' '{}'", listinoId);
        return listinoService.getOne(listinoId);
    }

    @RequestMapping(method = GET, path = "/{listinoId}/listini-prezzi")
    @CrossOrigin
    public List<ListinoPrezzo> getListiniPrezzi(@PathVariable final Long listinoId) {
        LOGGER.info("Performing GET request for retrieving 'listiniPrezzi' of 'listino' '{}'", listinoId);
        return listinoService.getListiniPrezziByListinoId(listinoId);
    }

    @RequestMapping(method = GET, path = "/{listinoId}/listini-prezzi-variazioni")
    @CrossOrigin
    public List<ListinoPrezzoVariazione> getListiniPrezziVariazioni(@PathVariable final Long listinoId) {
        LOGGER.info("Performing GET request for retrieving 'listiniPrezziVariazioni' of 'listino' '{}'", listinoId);
        return listinoService.getListiniPrezziVariazioniByListinoId(listinoId);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public Listino create(@RequestBody final Listino listino){
        LOGGER.info("Performing POST request for creating 'listino'");
        return listinoService.create(listino);
    }

    @RequestMapping(method = PUT, path = "/{listinoId}")
    @CrossOrigin
    public Listino update(@PathVariable final Long listinoId, @RequestBody final Listino listino){
        LOGGER.info("Performing PUT request for updating 'listino' '{}'", listinoId);
        if (!Objects.equals(listinoId, listino.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return listinoService.update(listino);
    }

    @RequestMapping(method = POST, path = "/{listinoId}/listini-prezzi-variazioni")
    @ResponseStatus(CREATED)
    @CrossOrigin
    public void createListinoPrezziAndVariazioni(@PathVariable final Long listinoId, @RequestBody final List<ListinoPrezzoVariazione> listiniPrezziVariazioni){
        LOGGER.info("Performing POST request for creating 'listiniPrezziVariazioni'");
        Listino listino = listinoService.getOne(listinoId);
        if(listino != null){
            listinoService.createListiniVariazioniPrezzi(listino, listiniPrezziVariazioni);
            return;
        }
        throw new ResourceNotFoundException();
    }

    @RequestMapping(method = PUT, path = "/{listinoId}/listini-prezzi-variazioni")
    @CrossOrigin
    public void recreateListinoPrezziAndVariazioni(@PathVariable final Long listinoId, @RequestBody final List<ListinoPrezzoVariazione> listiniPrezziVariazioni){
        LOGGER.info("Performing PUT request for recreating 'listiniPrezziVariazioni'");
        Listino listino = listinoService.getOne(listinoId);
        if(listino != null){
            listinoService.recreateListiniVariazioniPrezzi(listino, listiniPrezziVariazioni);
            return;
        }
        throw new ResourceNotFoundException();
    }

    @RequestMapping(method = POST, path = "/{listinoId}/duplicate")
    @CrossOrigin
    public Listino duplicateListino(@PathVariable final Long listinoId, @RequestBody final Map<String, String> body){
        LOGGER.info("Performing POST request for duplicating listino '{}'", listinoId);
        return listinoService.duplicate(listinoId, body);
    }

    @RequestMapping(method = DELETE, path = "/{listinoId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long listinoId){
        LOGGER.info("Performing DELETE request for deleting 'listino' '{}'", listinoId);
        listinoService.delete(listinoId);
    }
}

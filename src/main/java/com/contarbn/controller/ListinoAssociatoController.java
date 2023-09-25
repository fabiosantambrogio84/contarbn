package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.ListinoAssociato;
import com.contarbn.service.ListinoAssociatoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path="/listini-associati")
public class ListinoAssociatoController {

    private static Logger LOGGER = LoggerFactory.getLogger(ListinoAssociatoController.class);

    private final ListinoAssociatoService listinoAssociatoService;

    @Autowired
    public ListinoAssociatoController(final ListinoAssociatoService listinoAssociatoService){
        this.listinoAssociatoService = listinoAssociatoService;
    }

    @RequestMapping(method = GET)
    @CrossOrigin
    public Set<ListinoAssociato> getAll() {
        LOGGER.info("Performing GET request for retrieving list of 'listiniAssociati'");
        return listinoAssociatoService.getAll();
    }

    @RequestMapping(method = GET, path = "/{listinoAssociatoId}")
    @CrossOrigin
    public ListinoAssociato getOne(@PathVariable final Long listinoAssociatoId) {
        LOGGER.info("Performing GET request for retrieving 'listinoAssociato' '{}'", listinoAssociatoId);
        return listinoAssociatoService.getOne(listinoAssociatoId);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public List<ListinoAssociato> create(@RequestBody final List<ListinoAssociato> listiniAssociati){
        LOGGER.info("Performing POST request for creating a list of 'listinoAssociato'");
        return listinoAssociatoService.create(listiniAssociati);
    }

    @RequestMapping(method = PUT, path = "/{listinoAssociatoId}")
    @CrossOrigin
    public ListinoAssociato update(@PathVariable final Long listinoAssociatoId, @RequestBody final ListinoAssociato listinoAssociato){
        LOGGER.info("Performing PUT request for updating 'listinoAssociato' '{}'", listinoAssociatoId);
        if (!Objects.equals(listinoAssociatoId, listinoAssociato.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return listinoAssociatoService.update(listinoAssociato);
    }

    @RequestMapping(method = DELETE, path = "/{listinoAssociatoId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long listinoAssociatoId){
        LOGGER.info("Performing DELETE request for deleting 'listinoAssociato' '{}'", listinoAssociatoId);
        listinoAssociatoService.delete(listinoAssociatoId);
    }
}

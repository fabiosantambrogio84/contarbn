package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.ListinoPrezzo;
import com.contarbn.service.ListinoPrezzoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path="/listini-prezzi")
public class ListinoPrezzoController {

    private static Logger LOGGER = LoggerFactory.getLogger(ListinoPrezzoController.class);

    private final ListinoPrezzoService listinoPrezzoService;

    @Autowired
    public ListinoPrezzoController(final ListinoPrezzoService listinoPrezzoService){
        this.listinoPrezzoService = listinoPrezzoService;
    }

    @RequestMapping(method = GET, path = "/{listinoPrezzoId}")
    @CrossOrigin
    public ListinoPrezzo getOne(@PathVariable final Long listinoPrezzoId) {
        LOGGER.info("Performing GET request for retrieving 'listinoPrezzo' '{}'", listinoPrezzoId);
        return listinoPrezzoService.getOne(listinoPrezzoId);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public List<ListinoPrezzo> create(@RequestBody final List<ListinoPrezzo> listiniPrezzi){
        LOGGER.info("Performing POST request for creating 'listiniPrezzi'");
        return listinoPrezzoService.create(listiniPrezzi);
    }

    @RequestMapping(method = PUT, path = "/{listinoPrezzoId}")
    @CrossOrigin
    public ListinoPrezzo update(@PathVariable final Long listinoPrezzoId, @RequestBody final ListinoPrezzo listinoPrezzo){
        LOGGER.info("Performing PUT request for updating 'listinoPrezzo' '{}'", listinoPrezzoId);
        if (!Objects.equals(listinoPrezzoId, listinoPrezzo.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return listinoPrezzoService.update(listinoPrezzo);
    }

    @RequestMapping(method = DELETE, path = "/{listinoPrezzoId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long listinoPrezzoId){
        LOGGER.info("Performing DELETE request for deleting 'listinoPrezzo' '{}'", listinoPrezzoId);
        listinoPrezzoService.delete(listinoPrezzoId);
    }
}

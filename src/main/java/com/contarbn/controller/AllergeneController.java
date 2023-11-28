package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.Allergene;
import com.contarbn.service.AllergeneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path="/allergeni")
@Slf4j
public class AllergeneController {

    private final AllergeneService allergeneService;

    public AllergeneController(final AllergeneService allergeneService){
        this.allergeneService = allergeneService;
    }

    @RequestMapping(method = GET)
    @CrossOrigin
    public List<Allergene> getAll() {
        log.info("Performing GET request for retrieving list of 'allergeni'");
        return allergeneService.getAll();
    }

    @RequestMapping(method = GET, path = "/{allergeneId}")
    @CrossOrigin
    public Allergene getOne(@PathVariable final Long allergeneId) {
        log.info("Performing GET request for retrieving 'allergene' '{}'", allergeneId);
        return allergeneService.getOne(allergeneId);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public Allergene create(@RequestBody final Allergene allergene){
        log.info("Performing POST request for creating 'allergene'");
        return allergeneService.create(allergene);
    }

    @RequestMapping(method = PUT, path = "/{allergeneId}")
    @CrossOrigin
    public Allergene update(@PathVariable final Long allergeneId, @RequestBody final Allergene allergene){
        log.info("Performing PUT request for updating 'allergene' '{}'", allergeneId);
        if (!Objects.equals(allergeneId, allergene.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return allergeneService.update(allergene);
    }

    @RequestMapping(method = DELETE, path = "/{allergeneId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long allergeneId){
        log.info("Performing DELETE request for deleting 'allergene' '{}'", allergeneId);
        allergeneService.delete(allergeneId);
    }
}
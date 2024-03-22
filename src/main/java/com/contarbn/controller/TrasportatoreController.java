package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.Trasportatore;
import com.contarbn.service.TrasportatoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path="/trasportatori")
@Slf4j
@RequiredArgsConstructor
public class TrasportatoreController {

    private final TrasportatoreService trasportatoreService;

    @RequestMapping(method = GET)
    @CrossOrigin
    public Set<Trasportatore> getAll(@RequestParam(name = "context", required = false) String context) {
        log.info("Performing GET request for retrieving list of 'trasportatori' with context {}", context);
        return trasportatoreService.getAll(context);
    }

    @RequestMapping(method = GET, path = "/{trasportatoreId}")
    @CrossOrigin
    public Trasportatore getOne(@PathVariable final Long trasportatoreId) {
        log.info("Performing GET request for retrieving 'trasportatore' '{}'", trasportatoreId);
        return trasportatoreService.getOne(trasportatoreId);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public Trasportatore create(@RequestBody final Trasportatore trasportatore){
        log.info("Performing POST request for creating 'trasportatore'");
        return trasportatoreService.create(trasportatore);
    }

    @RequestMapping(method = PUT, path = "/{trasportatoreId}")
    @CrossOrigin
    public Trasportatore update(@PathVariable final Long trasportatoreId, @RequestBody final Trasportatore trasportatore){
        log.info("Performing PUT request for updating 'trasportatore' '{}'", trasportatoreId);
        if (!Objects.equals(trasportatoreId, trasportatore.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return trasportatoreService.update(trasportatore);
    }

    @RequestMapping(method = DELETE, path = "/{trasportatoreId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long trasportatoreId){
        log.info("Performing DELETE request for deleting 'trasportatore' '{}'", trasportatoreId);
        trasportatoreService.delete(trasportatoreId);
    }
}
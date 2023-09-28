package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.*;
import com.contarbn.service.FornitoreService;
import com.contarbn.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RestController
@RequestMapping(path="/fornitori")
public class FornitoreController {

    private final FornitoreService fornitoreService;

    @Autowired
    public FornitoreController(final FornitoreService fornitoreService){
        this.fornitoreService = fornitoreService;
    }

    @RequestMapping(method = GET)
    @CrossOrigin
    public List<Fornitore> getAll(@RequestParam(name = "codiceTipo", required = false) String codiceTipoFornitore,
                                  @RequestParam(name = "attivo", required = false) Boolean active) {
        log.info("Performing GET request for retrieving list of 'fornitori'");
        log.info("Request params: codiceTipoFornitore {}, attivo {}",codiceTipoFornitore, active);

        Predicate<Fornitore> isFornitoreTipoFornitoreEquals = fornitore -> {
            if(codiceTipoFornitore != null){
                TipoFornitore tipoFornitore = fornitore.getTipoFornitore();
                if(tipoFornitore != null){
                    return tipoFornitore.getCodice().equals(codiceTipoFornitore);
                }
                return false;
            }
            return true;
        };
        Predicate<Fornitore> isFornitoreAttivoEquals = fornitore -> {
            if(active != null){
                return fornitore.getAttivo().equals(active);
            }
            return true;
        };
        return fornitoreService.getAll().stream()
                .filter(isFornitoreTipoFornitoreEquals.and(isFornitoreAttivoEquals))
                .sorted(Comparator.comparing(Fornitore::getRagioneSociale))
                .collect(Collectors.toList());
    }

    @RequestMapping(method = GET, path = "/{fornitoreId}")
    @CrossOrigin
    public Fornitore getOne(@PathVariable final Long fornitoreId) {
        log.info("Performing GET request for retrieving 'fornitore' '{}'", fornitoreId);
        return fornitoreService.getOne(fornitoreId);
    }

    @RequestMapping(method = GET, path = "/{fornitoreId}/articoli")
    @CrossOrigin
    public List<Articolo> getArticoli(@PathVariable final Long fornitoreId, @RequestParam(name = "attivo", required = false) Boolean active) {
        log.info("Performing GET request for retrieving 'articoli' of 'fornitore' '{}'", fornitoreId);
        log.info("Query parameter 'attivo' equal to '{}'", active);
        if(active != null){
            List<Articolo> articoli = fornitoreService.getOne(fornitoreId).getArticoli();
            return articoli.stream().filter(a -> {
                if(active != null){
                    return active.equals(a.getAttivo());
                }
                return true;
            }).collect(Collectors.toList());
        } else {
            return fornitoreService.getOne(fornitoreId).getArticoli();
        }
    }

    @RequestMapping(method = GET, path = "/default")
    @CrossOrigin
    public Fornitore getByRagioneSociale() {
        log.info("Performing GET request for retrieving default 'fornitore' ({})", Constants.DEFAULT_FORNITORE);
        return fornitoreService.getByRagioneSociale(Constants.DEFAULT_FORNITORE);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public Fornitore create(@RequestBody final Fornitore fornitore){
        log.info("Performing POST request for creating 'fornitore'");
        return fornitoreService.create(fornitore);
    }

    @RequestMapping(method = PUT, path = "/{fornitoreId}")
    @CrossOrigin
    public Fornitore update(@PathVariable final Long fornitoreId, @RequestBody final Fornitore fornitore){
        log.info("Performing PUT request for updating 'fornitore' '{}'", fornitoreId);
        if (!Objects.equals(fornitoreId, fornitore.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return fornitoreService.update(fornitore);
    }

    @RequestMapping(method = DELETE, path = "/{fornitoreId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long fornitoreId){
        log.info("Performing DELETE request for deleting 'fornitore' '{}'", fornitoreId);
        fornitoreService.delete(fornitoreId);
    }
}

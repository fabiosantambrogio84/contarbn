package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.DdtAcquisto;
import com.contarbn.model.Fornitore;
import com.contarbn.model.beans.DdtAcquistoRicercaLotto;
import com.contarbn.service.DdtAcquistoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RestController
@RequestMapping(path="/ddts-acquisto")
public class DdtAcquistoController {

    private final DdtAcquistoService ddtAcquistoService;

    @Autowired
    public DdtAcquistoController(final DdtAcquistoService ddtAcquistoService){
        this.ddtAcquistoService = ddtAcquistoService;
    }

    @RequestMapping(method = GET)
    @CrossOrigin
    public Set<DdtAcquisto> getAll(@RequestParam(name = "numero", required = false) String numero,
                                    @RequestParam(name = "fornitore", required = false) String fornitore) {
        log.info("Performing GET request for retrieving list of 'ddts acquisto'");
        log.info("Request params: numero {}, fornitore {}", numero, fornitore);

        Predicate<DdtAcquisto> isDdtAcquistoNumeroEquals = ddtAcquisto -> {
            if(numero != null){
                return ddtAcquisto.getNumero().equals(numero);
            }
            return true;
        };
        Predicate<DdtAcquisto> isDdtAcquistoFornitoreContains = ddtAcquisto -> {
            if(fornitore != null){
                Fornitore ddtAcquistoFornitore = ddtAcquisto.getFornitore();
                if(ddtAcquistoFornitore != null){
                    if(ddtAcquistoFornitore.getRagioneSociale() != null){
                        return (ddtAcquistoFornitore.getRagioneSociale().toLowerCase()).contains(fornitore.toLowerCase());
                    }else {
                        if(ddtAcquistoFornitore.getRagioneSociale2() != null){
                            return (ddtAcquistoFornitore.getRagioneSociale2().toLowerCase()).contains(fornitore.toLowerCase());
                        }
                    }
                }
                return false;
            }
            return true;
        };

        Set<DdtAcquisto> ddtsAcquisto = ddtAcquistoService.getAll();
        return ddtsAcquisto.stream().filter(isDdtAcquistoNumeroEquals
                .and(isDdtAcquistoFornitoreContains)).collect(Collectors.toSet());
    }

    @RequestMapping(method = GET, path = "/search-lotto")
    @CrossOrigin
    public Set<DdtAcquistoRicercaLotto> getAllByLotto(@RequestParam(name = "lotto") String lotto) {
        log.info("Performing GET request for retrieving list of 'ddts acquisto' filtered by lotto");
        log.info("Request params:lotto {}",lotto);

        return ddtAcquistoService.getAllByLotto(lotto);
    }

    @RequestMapping(method = GET, path = "/{ddtAcquistoId}")
    @CrossOrigin
    public DdtAcquisto getOne(@PathVariable final Long ddtAcquistoId) {
        log.info("Performing GET request for retrieving 'ddt acquisto' '{}'", ddtAcquistoId);
        return ddtAcquistoService.getOne(ddtAcquistoId);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public DdtAcquisto create(@RequestBody final DdtAcquisto ddtAcquisto){
        log.info("Performing POST request for creating 'ddt acquisto'");
        return ddtAcquistoService.create(ddtAcquisto);
    }

    @RequestMapping(method = PUT, path = "/{ddtAcquistoId}")
    @CrossOrigin
    public DdtAcquisto update(@PathVariable final Long ddtAcquistoId, @RequestBody final DdtAcquisto ddtAcquisto){
        log.info("Performing PUT request for updating 'ddt acquisto' '{}'", ddtAcquistoId);
        if (!Objects.equals(ddtAcquistoId, ddtAcquisto.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return ddtAcquistoService.update(ddtAcquisto);
    }

    @RequestMapping(method = DELETE, path = "/{ddtAcquistoId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long ddtAcquistoId,
                       @RequestParam(name = "modificaGiacenze", required = false) Boolean modificaGiacenze){
        log.info("Performing DELETE request for deleting 'ddt acquisto' '{}', modificaGiacenze={}", ddtAcquistoId, modificaGiacenze);
        ddtAcquistoService.delete(ddtAcquistoId, (modificaGiacenze != null ? modificaGiacenze : Boolean.FALSE));
    }

}

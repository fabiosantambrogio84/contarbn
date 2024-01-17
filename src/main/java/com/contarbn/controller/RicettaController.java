package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.Ricetta;
import com.contarbn.model.SchedaTecnica;
import com.contarbn.service.RicettaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path="/ricette")
public class RicettaController {

    private final RicettaService ricettaService;

    @RequestMapping(method = GET)
    @CrossOrigin
    public Set<Ricetta> getAll() {
        log.info("Performing GET request for retrieving list of 'ricette'");
        return ricettaService.getAll();
    }

    @RequestMapping(method = GET, path = "/{ricettaId}")
    @CrossOrigin
    public Ricetta getOne(@PathVariable final Long ricettaId) {
        log.info("Performing GET request for retrieving 'ricetta' '{}'", ricettaId);
        return ricettaService.getOne(ricettaId);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public Ricetta create(@RequestBody final Ricetta ricetta){
        log.info("Performing POST request for creating 'ricetta'");
        return ricettaService.create(ricetta);
    }

    @RequestMapping(method = PUT, path = "/{ricettaId}")
    @CrossOrigin
    public Ricetta update(@PathVariable final Long ricettaId, @RequestBody final Ricetta ricetta){
        log.info("Performing PUT request for updating 'ricetta' '{}'", ricettaId);
        if (!Objects.equals(ricettaId, ricetta.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return ricettaService.update(ricetta);
    }

    @RequestMapping(method = DELETE, path = "/{ricettaId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long ricettaId){
        log.info("Performing DELETE request for deleting 'ricetta' '{}'", ricettaId);
        ricettaService.delete(ricettaId);
    }

    @RequestMapping(method = GET, path = "/{ricettaId}/scheda-tecnica")
    @CrossOrigin
    public Object getSchedaTecnica(@PathVariable final Long ricettaId) {
        log.info("Performing GET request for retrieving 'scheda-tecnica' for 'ricetta' '{}'", ricettaId);
        return ricettaService.getSchedaTecnica(ricettaId);
    }

    @RequestMapping(method = GET, path = "/scheda-tecnica/num-revisione")
    @CrossOrigin
    public Map<String, Integer> getNumRevisioneAndAnno(@RequestParam(name = "data", required = false) Date data) {
        log.info("Performing GET request for retrieving 'num revisione' for 'scheda-tecnica' with date '{}'", data);
        return ricettaService.getSchedaTecnicaNumRevisione(data);
    }

    @RequestMapping(method = POST, path = "/{ricettaId}/scheda-tecnica")
    @ResponseStatus(CREATED)
    @CrossOrigin
    public SchedaTecnica saveSchedaTecnica(@PathVariable final Long ricettaId, @RequestBody final SchedaTecnica schedaTecnica){
        log.info("Performing POST request for creating 'scheda-tecnica' for 'ricetta' {}", ricettaId);
        return ricettaService.saveSchedaTecnica(schedaTecnica);
    }
}
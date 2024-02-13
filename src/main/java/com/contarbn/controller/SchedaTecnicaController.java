package com.contarbn.controller;

import com.contarbn.model.SchedaTecnica;
import com.contarbn.model.beans.SchedaTecnicaResponse;
import com.contarbn.service.SchedaTecnicaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path="/schede-tecniche")
public class SchedaTecnicaController {

    private final SchedaTecnicaService schedaTecnicaService;

    @RequestMapping(method = GET, path = "/num-revisione")
    @CrossOrigin
    public Map<String, Integer> getNumRevisioneAndAnno(@RequestParam(name = "data", required = false) Date data) {
        log.info("Performing GET request for retrieving 'num revisione' for 'scheda-tecnica' with date '{}'", data);
        return schedaTecnicaService.getNumRevisioneAndAnno(data);
    }

    @RequestMapping(method = GET, path = "/{produzioneId}/{articoloId}/scheda-tecnica")
    @CrossOrigin
    public SchedaTecnicaResponse getSchedaTecnica(@PathVariable final Long produzioneId,
                                                  @PathVariable final Long articoloId) {
        log.info("Performing GET request for retrieving 'scheda-tecnica' for 'produzione' '{}' and 'articolo' '{}'", produzioneId, articoloId);
        return schedaTecnicaService.getSchedaTecnica(produzioneId, articoloId);
    }

    @RequestMapping(method = POST, path = "/{produzioneId}/{articoloId}/scheda-tecnica")
    @ResponseStatus(CREATED)
    @CrossOrigin
    public SchedaTecnica saveSchedaTecnica(@PathVariable final Long produzioneId,
                                           @PathVariable final Long articoloId,
                                           @RequestBody final SchedaTecnica schedaTecnica){
        log.info("Performing POST request for creating 'scheda-tecnica' for 'produzione' '{}' and 'articolo' '{}'", produzioneId, articoloId);
        return schedaTecnicaService.save(schedaTecnica);
    }
}
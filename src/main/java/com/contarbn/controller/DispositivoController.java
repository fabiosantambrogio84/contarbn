package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.Dispositivo;
import com.contarbn.service.DispositivoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path="/configurazione/dispositivi")
public class DispositivoController {

    private final DispositivoService dispositivoService;

    @RequestMapping(method = GET)
    @CrossOrigin
    public Set<Dispositivo> getAll() {
        log.info("Performing GET request for retrieving list of 'dispositivi'");
        return dispositivoService.getAll();
    }

    @RequestMapping(method = GET, path = "/{idDispositivo}")
    @CrossOrigin
    public Dispositivo getOne(@PathVariable final Long idDispositivo) {
        log.info("Performing GET request for retrieving 'dispositivo' '{}'", idDispositivo);
        return dispositivoService.getOne(idDispositivo);
    }

    @RequestMapping(method = HEAD, path = "/{idDispositivo}/ping")
    @CrossOrigin
    public void ping(@PathVariable final Long idDispositivo) throws IOException {
        log.info("Performing HEAD request for pinging 'dispositivo' '{}'", idDispositivo);
        dispositivoService.ping(idDispositivo);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public Dispositivo create(@RequestBody final Dispositivo dispositivo){
        return dispositivoService.create(dispositivo);
    }

    @RequestMapping(method = PUT, path = "/{idDispositivo}")
    @CrossOrigin
    public Dispositivo update(@PathVariable final Long idDispositivo, @RequestBody final Dispositivo dispositivo){
        log.info("Performing PUT request for updating 'dispositivo' '{}'", idDispositivo);
        if (!Objects.equals(idDispositivo, dispositivo.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return dispositivoService.update(dispositivo);
    }
}

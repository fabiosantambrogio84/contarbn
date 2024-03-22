package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.Autista;
import com.contarbn.service.AutistaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@RestController
@RequestMapping(path="/autisti")
public class AutistaController {

    private final AutistaService autistaService;

    @RequestMapping(method = GET)
    @CrossOrigin
    public List<Autista> getAll(@RequestParam(name = "attivo", required = false) Boolean active,
                                @RequestParam(name = "context", required = false) String context) {
        log.info("Performing GET request for retrieving list of 'autisti' with attivo {}, context {}", active, context);

        Predicate<Autista> isAutistaAttivoEquals = autista -> {
            if(active != null){
                return autista.getAttivo().equals(active);
            }
            return true;
        };

        return autistaService.getAll(context).stream()
                .filter(isAutistaAttivoEquals)
                .sorted(Comparator.comparing(Autista::getCognome).thenComparing(Autista::getNome))
                .collect(Collectors.toList());
    }

    @RequestMapping(method = GET, path = "/{autistaId}")
    @CrossOrigin
    public Autista getOne(@PathVariable final Long autistaId) {
        log.info("Performing GET request for retrieving 'autista' '{}'", autistaId);
        return autistaService.getOne(autistaId);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public Autista create(@RequestBody final Autista autista){
        log.info("Performing POST request for creating 'autista'");
        return autistaService.create(autista);
    }

    @RequestMapping(method = PUT, path = "/{autistaId}")
    @CrossOrigin
    public Autista update(@PathVariable final Long autistaId, @RequestBody final Autista autista){
        log.info("Performing PUT request for updating 'autista' '{}'", autistaId);
        if (!Objects.equals(autistaId, autista.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return autistaService.update(autista);
    }

    @RequestMapping(method = DELETE, path = "/{autistaId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long autistaId){
        log.info("Performing DELETE request for deleting 'autista' '{}'", autistaId);
        autistaService.delete(autistaId);
    }
}

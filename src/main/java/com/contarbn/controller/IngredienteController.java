package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.Fornitore;
import com.contarbn.model.Ingrediente;
import com.contarbn.service.IngredienteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RestController
@RequestMapping(path="/ingredienti")
public class IngredienteController {

    private final IngredienteService ingredienteService;

    @Autowired
    public IngredienteController(final IngredienteService ingredienteService){
        this.ingredienteService = ingredienteService;
    }

    @RequestMapping(method = GET)
    @CrossOrigin
    public List<Ingrediente> getAll(@RequestParam(name = "attivo", required = false) Boolean active,
                                    @RequestParam(name = "idFornitore", required = false) Integer idFornitore) {
        log.info("Performing GET request for retrieving list of 'ingredienti'");
        log.info("Query parameter: 'attivo' '{}', 'idFornitore' '{}'", active, idFornitore);

        Predicate<Ingrediente> isIngredienteActiveEquals = ingrediente -> {
            if(active != null){
                return ingrediente.getAttivo().equals(active);
            }
            return true;
        };

        Predicate<Ingrediente> isIngredienteIdFornitoreEquals = ingrediente -> {
            if(idFornitore != null){
                Fornitore ingredienteFornitore = ingrediente.getFornitore();
                if(ingredienteFornitore != null){
                    return ingredienteFornitore.getId().equals(Long.valueOf(idFornitore));
                }
                return false;
            }
            return true;
        };
        Set<Ingrediente> ingredienti = ingredienteService.getAll().stream()
                .filter(isIngredienteIdFornitoreEquals
                        .and(isIngredienteActiveEquals))
                .collect(Collectors.toSet());
        return ingredienti.stream().sorted(Comparator.comparing(Ingrediente::getCodice)).collect(Collectors.toList());
    }

    @RequestMapping(method = GET, path = "/{ingredienteId}")
    @CrossOrigin
    public Ingrediente getOne(@PathVariable final Long ingredienteId) {
        log.info("Performing GET request for retrieving 'ingrediente' '{}'", ingredienteId);
        return ingredienteService.getOne(ingredienteId);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public Ingrediente create(@RequestBody final Ingrediente ingrediente){
        log.info("Performing POST request for creating 'ingrediente'");
        return ingredienteService.create(ingrediente);
    }

    @RequestMapping(method = PUT, path = "/{ingredienteId}")
    @CrossOrigin
    public Ingrediente update(@PathVariable final Long ingredienteId, @RequestBody final Ingrediente ingrediente){
        log.info("Performing PUT request for updating 'ingrediente' '{}'", ingredienteId);
        if (!Objects.equals(ingredienteId, ingrediente.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return ingredienteService.update(ingrediente);
    }

    @RequestMapping(method = DELETE, path = "/{ingredienteId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long ingredienteId){
        log.info("Performing DELETE request for deleting 'ingrediente' '{}'", ingredienteId);
        ingredienteService.delete(ingredienteId);
    }
}

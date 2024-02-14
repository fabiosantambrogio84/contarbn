package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.*;
import com.contarbn.repository.ClienteArticoloRepository;
import com.contarbn.service.ArticoloService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@RequiredArgsConstructor
@RestController
@RequestMapping(path="/articoli")
public class ArticoloController {

    private final ArticoloService articoloService;
    private final ClienteArticoloRepository clienteArticoloRepository;


    @RequestMapping(method = GET)
    @CrossOrigin
    public List<Articolo> getAll(@RequestParam(name = "attivo", required = false) Boolean active,
                                 @RequestParam(name = "idFornitore", required = false) Integer idFornitore,
                                 @RequestParam(name = "barcode", required = false) String barcode,
                                 @RequestParam(name = "idCliente", required = false) Integer idCliente) {
        log.info("Performing GET request for retrieving list of 'articoli'");
        log.info("Query parameter: 'attivo' '{}', 'idFornitore' '{}', 'barcode' '{}', 'idCliente' '{}'", active, idFornitore, barcode, idCliente);

        Predicate<Articolo> isArticoloIdFornitoreEquals = articolo -> {
            if(idFornitore != null){
                Fornitore articoloFornitore = articolo.getFornitore();
                if(articoloFornitore != null){
                    return articoloFornitore.getId().equals(Long.valueOf(idFornitore));
                }
                return false;
            }
            return true;
        };

        Set<Articolo> articoli;
        if(active != null && !StringUtils.isEmpty(barcode)){
            articoli = articoloService.getAllByAttivoAndBarcode(active, barcode);
        } else {
            if(idCliente != null){
                Set<ClienteArticolo> clienteArticoli = clienteArticoloRepository.findByClienteId(idCliente.longValue());
                if(clienteArticoli != null && !clienteArticoli.isEmpty()){
                    articoli = clienteArticoli.stream().map(ClienteArticolo::getArticolo).collect(Collectors.toSet());
                } else {
                    articoli = articoloService.getAllByAttivoOrderByCodiceAsc(true);
                }
            } else if(active != null){
                articoli = articoloService.getAllByAttivoOrderByCodiceAsc(active);
            } else {
                articoli = articoloService.getAll();
            }
        }
        return articoli.stream().filter(isArticoloIdFornitoreEquals).sorted(Comparator.comparing(Articolo::getCodice)).collect(Collectors.toList());
    }

    @RequestMapping(method = GET, path = "/{articoloId}")
    @CrossOrigin
    public Articolo getOne(@PathVariable final Long articoloId) {
        log.info("Performing GET request for retrieving 'articolo' '{}'", articoloId);
        return articoloService.getOne(articoloId);
    }

    @RequestMapping(method = GET, path = "/codice/{codice}/like")
    @CrossOrigin
    public List<Articolo> getByCodiceLike(@PathVariable final String codice,
                                          @RequestParam(name = "attivo", required = false) Boolean active) {
        log.info("Performing GET request for retrieving 'articolo' with codice like '{}' and active '{}'", codice, active);
        return articoloService.getByCodiceLike(codice, active);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public Articolo create(@RequestBody final Articolo articolo){
        log.info("Performing POST request for creating 'articolo'");
        return articoloService.create(articolo);
    }

    @RequestMapping(method = PUT, path = "/{articoloId}")
    @CrossOrigin
    public Articolo update(@PathVariable final Long articoloId, @RequestBody final Articolo articolo){
        log.info("Performing PUT request for updating 'articolo' '{}'", articoloId);
        if (!Objects.equals(articoloId, articolo.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return articoloService.update(articolo);
    }

    @RequestMapping(method = DELETE, path = "/{articoloId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long articoloId){
        log.info("Performing DELETE request for deleting 'articolo' '{}'", articoloId);
        articoloService.delete(articoloId);
    }

    @RequestMapping(method = GET, path = "/{articoloId}/immagini")
    @CrossOrigin
    public List<ArticoloImmagine> getArticoloImmagini(@PathVariable final Long articoloId) {
        log.info("Performing GET request for retrieving 'articoloImmagini' of 'articolo' '{}'", articoloId);
        return articoloService.getArticoloImmagini(articoloId);
    }

    @RequestMapping(method = GET, path = "/{articoloId}/listini-prezzi")
    @CrossOrigin
    public List<ListinoPrezzo> getArticoloListiniPrezzi(@PathVariable final Long articoloId) {
        log.info("Performing GET request for retrieving 'listiniPrezzi' of 'articolo' '{}'", articoloId);
        return articoloService.getArticoloListiniPrezzi(articoloId);
    }
}
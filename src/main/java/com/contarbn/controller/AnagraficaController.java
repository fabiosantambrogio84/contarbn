package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.Anagrafica;
import com.contarbn.service.AnagraficaService;
import com.contarbn.util.enumeration.TipologiaAnagrafica;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path="/anagrafiche")
public class AnagraficaController {

    private final AnagraficaService anagraficaService;

    @RequestMapping(method = GET, path = "/analisi-microbiologiche")
    @CrossOrigin
    public List<Anagrafica> getAnalisiMicrobiologiche(@RequestParam(name = "attivo", required = false) Boolean active) {
        return anagraficaService.getAllByTipoAndAttivo(TipologiaAnagrafica.ANALISI_MICROBIOLOGICA, active);
    }

    @RequestMapping(method = GET, path = "/imballi")
    @CrossOrigin
    public List<Anagrafica> getImballi(@RequestParam(name = "attivo", required = false) Boolean active) {
        return anagraficaService.getAllByTipoAndAttivo(TipologiaAnagrafica.IMBALLO, active);
    }

    @RequestMapping(method = GET, path = "/materiali")
    @CrossOrigin
    public List<Anagrafica> getMateriali(@RequestParam(name = "attivo", required = false) Boolean active) {
        return anagraficaService.getAllByTipoAndAttivo(TipologiaAnagrafica.MATERIALE, active);
    }

    @RequestMapping(method = GET, path = "/nutrienti")
    @CrossOrigin
    public List<Anagrafica> getNutrienti(@RequestParam(name = "attivo", required = false) Boolean active) {
        return anagraficaService.getAllByTipoAndAttivo(TipologiaAnagrafica.NUTRIENTE, active);
    }

    @RequestMapping(method = GET, path = "/raccolte-differenziate")
    @CrossOrigin
    public List<Anagrafica> getRaccolteDifferenziate(@RequestParam(name = "attivo", required = false) Boolean active) {
        return anagraficaService.getAllByTipoAndAttivo(TipologiaAnagrafica.RACCOLTA_DIFFERENZIATA, active);
    }

    @RequestMapping(method = GET, path = "/tipologie-confezionamento")
    @CrossOrigin
    public List<Anagrafica> getTipologieConfezionamento(@RequestParam(name = "attivo", required = false) Boolean active) {
        return anagraficaService.getAllByTipoAndAttivo(TipologiaAnagrafica.TIPOLOGIA_CONFEZIONAMENTO, active);
    }

    @RequestMapping(method = GET, path = "/{anagraficaId}")
    @CrossOrigin
    public Anagrafica getById(@PathVariable final Long anagraficaId) {
        return anagraficaService.getById(anagraficaId);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public Anagrafica create(@RequestBody final Anagrafica anagrafica){
        return anagraficaService.save(anagrafica);
    }

    @RequestMapping(method = PUT, path = "/{anagraficaId}")
    @CrossOrigin
    public Anagrafica update(@PathVariable final Long anagraficaId, @RequestBody final Anagrafica anagrafica){
        if (!Objects.equals(anagraficaId, anagrafica.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return anagraficaService.save(anagrafica);
    }

    @RequestMapping(method = DELETE, path = "/{anagraficaId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long anagraficaId){
        anagraficaService.deleteById(anagraficaId);
    }
}
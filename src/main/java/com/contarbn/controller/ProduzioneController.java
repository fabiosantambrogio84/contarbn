package com.contarbn.controller;

import com.contarbn.model.Articolo;
import com.contarbn.model.Produzione;
import com.contarbn.model.ProduzioneConfezione;
import com.contarbn.model.beans.PageResponse;
import com.contarbn.model.views.VProduzione;
import com.contarbn.model.views.VProduzioneConfezioneEtichetta;
import com.contarbn.service.ProduzioneService;
import com.contarbn.util.ResponseUtils;
import com.contarbn.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path="/produzioni")
public class ProduzioneController {

    private final ProduzioneService produzioneService;

    @RequestMapping(method = GET, path = "/search")
    @CrossOrigin
    public PageResponse search(@RequestParam(name = "draw", required = false) Integer draw,
                               @RequestParam(name = "start", required = false) Integer start,
                               @RequestParam(name = "length", required = false) Integer length,
                               @RequestParam(name = "codice", required = false) Integer codice,
                               @RequestParam(name = "ricetta", required = false) String ricetta,
                               @RequestParam(name = "barcodeEan13", required = false) String barcodeEan13,
                               @RequestParam(name = "barcodeEan128", required = false) String barcodeEan128,
                               @RequestParam Map<String,String> allRequestParams) {
        log.info("Performing GET request for retrieving list of 'produzioni'");
        log.info("Request params: draw {}, start {}, length {}, codice {}, ricetta {}, barcodeEan13 {}, barcodeEan128 {}", draw, start, length, codice, ricetta, barcodeEan13, barcodeEan128);

        List<VProduzione> data = produzioneService.getAllByFilters(draw, start, length, Utils.getSortOrders(allRequestParams), codice, ricetta, barcodeEan13, barcodeEan128);
        Integer recordsCount = produzioneService.getCountByFilters(codice, ricetta, barcodeEan13, barcodeEan128);

        return ResponseUtils.createPageResponse(draw, recordsCount, recordsCount, data);
    }

    @RequestMapping(method = GET, path = "/search-lotto")
    @CrossOrigin
    public Set<VProduzione> searchByLotto(@RequestParam(name = "lotto") String lotto) {
        log.info("Performing GET request for retrieving list of 'produzioni' filtered by 'lotto' {}", lotto);
        return produzioneService.getAllByLotto(lotto);
    }

    @RequestMapping(method = GET, path = "/{produzioneId}")
    @CrossOrigin
    public Produzione getOne(@PathVariable final Long produzioneId) {
        log.info("Performing GET request for retrieving 'produzione' '{}'", produzioneId);
        return produzioneService.getOne(produzioneId);
    }

    @RequestMapping(method = GET, path = "/check-articolo")
    @CrossOrigin
    public Articolo checkArticolo(@RequestParam(name = "codiceRicetta") String codiceRicetta,
                                  @RequestParam(name = "idConfezione") Long idConfezione) {
        log.info("Performing GET request for checking if articolo for 'codiceRicetta' '{}' and 'idConfezione' '{}' exists", codiceRicetta, idConfezione);
        return produzioneService.checkArticolo(codiceRicetta, idConfezione);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public Produzione create(@RequestBody final Produzione produzione){
        log.info("Performing POST request for creating 'produzione'");
        return produzioneService.create(produzione);
    }

    @RequestMapping(method = DELETE, path = "/{produzioneId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long produzioneId){
        log.info("Performing DELETE request for deleting 'produzione' '{}'", produzioneId);
        produzioneService.delete(produzioneId);
    }

    @RequestMapping(method = GET, path = "/{produzioneId}/confezioni")
    @CrossOrigin
    public Set<ProduzioneConfezione> getConfezioni(@PathVariable final Long produzioneId) {
        log.info("Performing GET request for retrieving 'produzioneConfezioni' for produzione '{}'", produzioneId);
        return produzioneService.getProduzioneConfezioni(produzioneId);
    }

    @RequestMapping(method = GET, path = "/etichetta")
    @CrossOrigin
    public VProduzioneConfezioneEtichetta getProduzioneConfezioneEtichetta(@RequestParam(name = "idProduzioneConfezione") Long idProduzioneConfezione) {
        log.info("Performing GET request for retrieving data for 'etichetta' of 'produzione-confezione' '{}'", idProduzioneConfezione);
        return produzioneService.getProduzioneConfezioneEtichetta(idProduzioneConfezione);
    }
}

package com.contarbn.controller;

import com.contarbn.model.request.EtichettaRequest;
import com.contarbn.service.EtichettaService;
import com.contarbn.util.Constants;
import com.contarbn.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RestController
@RequestMapping(path="/etichette")
public class EtichettaController {

    private final EtichettaService etichettaService;

    public EtichettaController(final EtichettaService etichettaService){
        this.etichettaService = etichettaService;
    }

    @RequestMapping(method = POST, path="/stampa")
    @ResponseStatus(OK)
    @CrossOrigin
    public void stampa(@RequestBody final EtichettaRequest etichettaRequest) throws Exception{
        log.info("Performing POST request for printing 'etichetta'");

        etichettaService.stampa(etichettaRequest);

        log.info("Successfully printed 'etichetta'");
    }

    @RequestMapping(method = POST, path = "/download")
    @ResponseStatus(OK)
    @CrossOrigin
    public ResponseEntity<Resource> download(@RequestBody final EtichettaRequest etichettaRequest) throws Exception {
        log.info("Performing POST request for downloading 'etichetta'");

        Map<String, Object> result = etichettaService.generate(etichettaRequest.getArticolo(), etichettaRequest.getIngredienti(), etichettaRequest.getTracce(), etichettaRequest.getConservazione(), etichettaRequest.getValoriNutrizionali(), etichettaRequest.getDataConsumazione(), etichettaRequest.getLotto(), etichettaRequest.getPeso(), etichettaRequest.getBarcodeEan13(), etichettaRequest.getBarcodeEan128(), etichettaRequest.getIdDispositivo());

        byte[] fileContent = (byte[])result.get("fileContent");

        ByteArrayResource resource = new ByteArrayResource(fileContent);

        return ResponseEntity.ok()
                .headers(Utils.createHttpHeaders((String)result.get("fileName")))
                .contentLength(fileContent.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_TXT))
                .body(resource);
    }

    @RequestMapping(method = DELETE, path = "/{etichettaId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final String etichettaId){
        log.info("Performing DELETE request for deleting 'etichetta' '{}'", etichettaId);
        etichettaService.delete(etichettaId);
    }

}
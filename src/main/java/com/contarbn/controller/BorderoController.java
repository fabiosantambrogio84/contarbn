package com.contarbn.controller;

import com.contarbn.exception.GenericException;
import com.contarbn.model.beans.PageResponse;
import com.contarbn.service.BorderoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path="/bordero")
public class BorderoController {

    private final BorderoService borderoService;

    @RequestMapping(method = GET, path = "/genera")
    @CrossOrigin
    public PageResponse genera(@RequestParam(name = "draw", required = false) Integer draw,
                               @RequestParam(name = "start", required = false) Integer start,
                               @RequestParam(name = "length", required = false) Integer length,
                               @RequestParam(name = "autista") String autista,
                               @RequestParam(name = "dataConsegna") Date dataConsegna,
                               @RequestParam Map<String,String> allRequestParams) {
        log.info("Performing GET request for generating 'bordero'");
        log.info("Request params: draw {}, start {}, length {}, autista {}, dataConsegna {}",
                draw, start, length, autista, dataConsegna);

        Long idBordero = borderoService.create(autista, dataConsegna);
        if(idBordero == null){
            throw new GenericException("Error creating 'bordero' for autista '"+autista+"' and dataConsegna '"+dataConsegna+"'");
        }

        //List<VDdt> data = ddtService.getAllByFilters(draw, start, length, Utils.getSortOrders(allRequestParams), dataDa, dataA, progressivo, idCliente, cliente, idAgente, idAutista, idStato, pagato, fatturato, importo, idTipoPagamento, idArticolo);
        //Integer recordsCount = ddtService.getCountByFilters(dataDa, dataA, progressivo, idCliente, cliente, idAgente, idAutista, idStato, pagato, fatturato, importo, idTipoPagamento, idArticolo);

        //return ResponseUtils.createPageResponse(draw, recordsCount, recordsCount, data);

        return null;
    }

}

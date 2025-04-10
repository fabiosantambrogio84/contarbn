package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.exception.GenericException;
import com.contarbn.model.beans.PageResponse;
import com.contarbn.model.views.VBorderoRiga;
import com.contarbn.service.BorderoService;
import com.contarbn.util.ResponseUtils;
import com.contarbn.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

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

        List<VBorderoRiga> borderoRighe = borderoService.getAllByIdBordero(draw, start, length, Utils.getSortOrders(allRequestParams), idBordero);
        Integer recordsCount = borderoService.getCountByIdBordero(idBordero);

        return ResponseUtils.createPageResponse(draw, recordsCount, recordsCount, borderoRighe);
    }

    @RequestMapping(method = PATCH, path = "/righe/{idBorderoRiga}")
    @CrossOrigin
    public VBorderoRiga patch(@PathVariable final String idBorderoRiga, @RequestBody final Map<String,Object> patchBorderoRiga){
        log.info("Performing PATCH request for updating 'bordero riga' '{}'", idBorderoRiga);
        String id = (String)patchBorderoRiga.get("uuid");
        if (!Objects.equals(idBorderoRiga, id)) {
            throw new CannotChangeResourceIdException();
        }
        return borderoService.patch(patchBorderoRiga);
    }

    @RequestMapping(method = DELETE, path = "/righe/{idBorderoRiga}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final String idBorderoRiga){
        log.info("Performing DELETE request for deleting 'bordero riga' '{}'", idBorderoRiga);
        borderoService.deleteBorderoRiga(idBorderoRiga);
    }

}

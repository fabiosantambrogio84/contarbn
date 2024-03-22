package com.contarbn.controller;

import com.contarbn.model.SchedaTecnica;
import com.contarbn.model.beans.PageResponse;
import com.contarbn.model.beans.SchedaTecnicaResponse;
import com.contarbn.model.views.VSchedaTecnicaLight;
import com.contarbn.service.SchedaTecnicaService;
import com.contarbn.service.StampaService;
import com.contarbn.util.Constants;
import com.contarbn.util.ResponseUtils;
import com.contarbn.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path="/schede-tecniche")
public class SchedaTecnicaController {

    private final SchedaTecnicaService schedaTecnicaService;


    @RequestMapping(method = GET, path = "/search")
    @CrossOrigin
    public PageResponse search(@RequestParam(name = "draw", required = false) Integer draw,
                               @RequestParam(name = "start", required = false) Integer start,
                               @RequestParam(name = "length", required = false) Integer length,
                               @RequestParam(name = "prodotto", required = false) String prodotto,
                               @RequestParam Map<String,String> allRequestParams) {
        log.info("Performing GET request for retrieving list of 'schede-tecniche'");
        log.info("Request params: draw {}, start {}, length {}, prodotto {}", draw, start, length, prodotto);

        List<VSchedaTecnicaLight> data = schedaTecnicaService.getAllByFilters(draw, start, length, Utils.getSortOrders(allRequestParams), prodotto);
        Integer recordsCount = schedaTecnicaService.getCountByFilters(prodotto);

        return ResponseUtils.createPageResponse(draw, recordsCount, recordsCount, data);
    }

    @RequestMapping(method = GET, path = "/num-revisione")
    @CrossOrigin
    public Map<String, Integer> getNumRevisioneAndAnno(@RequestParam(name = "data", required = false) Date data) {
        log.info("Performing GET request for retrieving 'num revisione' for 'scheda-tecnica' with date '{}'", data);
        return schedaTecnicaService.getNumRevisioneAndAnno(data);
    }

    @RequestMapping(method = GET, path = "/{produzioneId}/{articoloId}/scheda-tecnica")
    @CrossOrigin
    public SchedaTecnicaResponse get(@PathVariable final Long produzioneId,
                                                  @PathVariable final Long articoloId) {
        log.info("Performing GET request for retrieving 'scheda-tecnica' for 'produzione' '{}' and 'articolo' '{}'", produzioneId, articoloId);
        return schedaTecnicaService.get(produzioneId, articoloId);
    }

    @RequestMapping(method = POST, path = "/{produzioneId}/{articoloId}/scheda-tecnica")
    @ResponseStatus(CREATED)
    @CrossOrigin
    public SchedaTecnica save(@PathVariable final Long produzioneId,
                                           @PathVariable final Long articoloId,
                                           @RequestBody final SchedaTecnica schedaTecnica){
        log.info("Performing POST request for creating 'scheda-tecnica' for 'produzione' '{}' and 'articolo' '{}'", produzioneId, articoloId);
        return schedaTecnicaService.save(schedaTecnica);
    }

    @RequestMapping(method = DELETE, path = "/{idSchedaTecnica}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long idSchedaTecnica){
        log.info("Performing DELETE request for deleting 'scheda-tecnica' '{}'", idSchedaTecnica);
        schedaTecnicaService.delete(idSchedaTecnica);
    }

    @RequestMapping(method = GET, path = "/{idSchedaTecnica}/pdf")
    @CrossOrigin
    public ResponseEntity<Resource> getPdf(@PathVariable final Long idSchedaTecnica){
        log.info("Performing GET request for creating pdf of 'scheda-tecnica' '{}''", idSchedaTecnica);

        byte[] reportBytes = schedaTecnicaService.getById(idSchedaTecnica).getPdf();

        ByteArrayResource resource = new ByteArrayResource(reportBytes);

        log.info("Successfully create pdf for 'scheda-tecnica' with id '{}'", idSchedaTecnica);

        return ResponseEntity.ok()
                .headers(StampaService.createHttpHeaders("scheda-tecnica-"+idSchedaTecnica+".pdf"))
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType(Constants.MEDIA_TYPE_APPLICATION_PDF))
                .body(resource);
    }
}
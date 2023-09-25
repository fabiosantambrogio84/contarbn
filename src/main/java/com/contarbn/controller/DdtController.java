package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.Ddt;
import com.contarbn.model.beans.DdtRicercaLotto;
import com.contarbn.model.beans.PageResponse;
import com.contarbn.model.views.VDdt;
import com.contarbn.service.DdtService;
import com.contarbn.util.ResponseUtils;
import com.contarbn.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@SuppressWarnings({"unused"})
@RestController
@RequestMapping(path="/ddts")
public class DdtController {

    private final static Logger LOGGER = LoggerFactory.getLogger(DdtController.class);

    private final DdtService ddtService;

    @Autowired
    public DdtController(final DdtService ddtService){
        this.ddtService = ddtService;
    }

    @RequestMapping(method = GET)
    @CrossOrigin
    public Set<Ddt> getAll() {
        LOGGER.info("Performing GET request for retrieving list of 'ddts'");

        return ddtService.getAll();
    }

    @RequestMapping(method = GET, path = "/search-lotto")
    @CrossOrigin
    public Set<DdtRicercaLotto> getAllByLotto(@RequestParam(name = "lotto") String lotto) {
        LOGGER.info("Performing GET request for retrieving list of 'ddts'");
        LOGGER.info("Request params: lotto {}", lotto);

        return ddtService.getAllByLotto(lotto);
    }

    @RequestMapping(method = GET, path = "/search")
    @CrossOrigin
    public PageResponse search(@RequestParam(name = "draw", required = false) Integer draw,
                             @RequestParam(name = "start", required = false) Integer start,
                             @RequestParam(name = "length", required = false) Integer length,
                             @RequestParam(name = "dataDa", required = false) Date dataDa,
                             @RequestParam(name = "dataA", required = false) Date dataA,
                             @RequestParam(name = "progressivo", required = false) Integer progressivo,
                             @RequestParam(name = "importo", required = false) Float importo,
                             @RequestParam(name = "tipoPagamento", required = false) Integer idTipoPagamento,
                             @RequestParam(name = "cliente", required = false) String cliente,
                             @RequestParam(name = "agente", required = false) Integer idAgente,
                             @RequestParam(name = "autista", required = false) Integer idAutista,
                             @RequestParam(name = "articolo", required = false) Integer idArticolo,
                             @RequestParam(name = "stato", required = false) Integer idStato,
                             @RequestParam(name = "pagato", required = false) Boolean pagato,
                             @RequestParam(name = "idCliente", required = false) Integer idCliente,
                             @RequestParam(name = "fatturato", required = false) Boolean fatturato,
                             @RequestParam Map<String,String> allRequestParams) {
        LOGGER.info("Performing GET request for searching list of 'ddts'");
        LOGGER.info("Request params: draw {}, start {}, length {}, dataDa {}, dataA {}, progressivo {}, importo {}, tipoPagamento {}, cliente {}, agente {}, autista {}, articolo {}, stato {}, pagato {}, idCliente {}, fatturato {}",
                draw, start, length, dataDa, dataA, progressivo, importo, idTipoPagamento, cliente, idAgente, idAutista, idArticolo, idStato, pagato, idCliente, fatturato);
        // /contafood-be/ddts?dataDa=&dataA=&progressivo=&importo=&tipoPagamento=&cliente=&agente=&autista=&articolo=

        List<VDdt> data = ddtService.getAllByFilters(draw, start, length, Utils.getSortOrders(allRequestParams), dataDa, dataA, progressivo, idCliente, cliente, idAgente, idAutista, idStato, pagato, fatturato, importo, idTipoPagamento, idArticolo);
        Integer recordsCount = ddtService.getCountByFilters(dataDa, dataA, progressivo, idCliente, cliente, idAgente, idAutista, idStato, pagato, fatturato, importo, idTipoPagamento, idArticolo);

        return ResponseUtils.createPageResponse(draw, recordsCount, recordsCount, data);
    }

    @RequestMapping(method = GET, path = "/{ddtId}")
    @CrossOrigin
    public Ddt getOne(@PathVariable final Long ddtId) {
        LOGGER.info("Performing GET request for retrieving 'ddt' '{}'", ddtId);
        return ddtService.getOne(ddtId);
    }

    @RequestMapping(method = GET, path = "/progressivo")
    @CrossOrigin
    public Map<String, Integer> getAnnoContabileAndProgressivo() {
        LOGGER.info("Performing GET request for retrieving 'annoContabile' and 'progressivo' for a new ddt");
        return ddtService.getAnnoContabileAndProgressivo();
    }

    @RequestMapping(method = GET, path = "/progressivi-duplicates")
    @CrossOrigin
    public Map<String, String> getProgressiviDuplicates() {
        LOGGER.info("Performing GET request for retrieving list of 'ddt.progressivo' duplicates");
        return ddtService.getProgressiviDuplicates();
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    @CrossOrigin
    public Ddt create(@RequestBody final Ddt ddt){
        LOGGER.info("Performing POST request for creating 'ddt'");
        return ddtService.create(ddt);
    }

    @RequestMapping(method = PUT, path = "/{ddtId}")
    @CrossOrigin
    public Ddt update(@PathVariable final Long ddtId, @RequestBody final Ddt ddt){
        LOGGER.info("Performing PUT request for updating 'ddt' '{}'", ddtId);
        if (!Objects.equals(ddtId, ddt.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return ddtService.update(ddt);
    }

    @RequestMapping(method = PATCH, path = "/{ddtId}")
    @CrossOrigin
    public Ddt patch(@PathVariable final Long ddtId, @RequestBody final Map<String,Object> patchDdt){
        LOGGER.info("Performing PATCH request for updating 'ddt' '{}'", ddtId);
        Long id = Long.valueOf((Integer) patchDdt.get("id"));
        if (!Objects.equals(ddtId, id)) {
            throw new CannotChangeResourceIdException();
        }
        return ddtService.patch(patchDdt);
    }

    @RequestMapping(method = DELETE, path = "/{ddtId}")
    @ResponseStatus(NO_CONTENT)
    @CrossOrigin
    public void delete(@PathVariable final Long ddtId,
                       @RequestParam(name = "modificaGiacenze", required = false) Boolean modificaGiacenze){
        LOGGER.info("Performing DELETE request for deleting 'ddt' '{}'", ddtId);
        LOGGER.info("Request params: modificaGiacenze={}", modificaGiacenze);
        ddtService.delete(ddtId, (modificaGiacenze != null ? modificaGiacenze : Boolean.FALSE));
    }

}

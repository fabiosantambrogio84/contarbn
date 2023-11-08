package com.contarbn.controller;

import com.contarbn.exception.CannotChangeResourceIdException;
import com.contarbn.model.DittaInfo;
import com.contarbn.model.beans.DittaInfoSingleton;
import com.contarbn.service.DittaInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Slf4j
@RestController
@RequestMapping(path="/ditta-info")
public class DittaInfoController {

    private final DittaInfoService dittaInfoService;

    public DittaInfoController(final DittaInfoService dittaInfoService){
        this.dittaInfoService = dittaInfoService;
    }

    @RequestMapping(method = GET)
    @CrossOrigin
    public List<DittaInfo> getAll() {
        log.info("Performing GET request for retrieving list of 'ditta-info'");
        return dittaInfoService.getAll();
    }

    @RequestMapping(method = GET, path = "/map")
    @CrossOrigin
    public Map<String, DittaInfo> getMap() {
        log.info("Performing GET request for retrieving the map of 'ditta-info'");
        return DittaInfoSingleton.get().getDittaInfoMap();
    }

    @RequestMapping(method = GET, path = "/{dittaInfoId}")
    @CrossOrigin
    public DittaInfo getOne(@PathVariable final Long dittaInfoId) {
        log.info("Performing GET request for retrieving 'ditta-info' '{}'", dittaInfoId);
        return dittaInfoService.getOne(dittaInfoId);
    }

    @RequestMapping(method = PUT, path = "/{dittaInfoId}")
    @CrossOrigin
    public DittaInfo update(@PathVariable final Long dittaInfoId, @RequestBody final DittaInfo dittaInfo){
        log.info("Performing PUT request for updating 'ditta-info' '{}'", dittaInfoId);
        if (!Objects.equals(dittaInfoId, dittaInfo.getId())) {
            throw new CannotChangeResourceIdException();
        }
        return dittaInfoService.update(dittaInfo);
    }

}
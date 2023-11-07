package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.DittaInfo;
import com.contarbn.repository.DittaInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
public class DittaInfoService {

    private final DittaInfoRepository dittaInfoRepository;

    @Autowired
    public DittaInfoService(final DittaInfoRepository dittaInfoRepository){
        this.dittaInfoRepository = dittaInfoRepository;
    }

    public List<DittaInfo> getAll(){
        log.info("Retrieving the list of 'ditta-info'");
        List<DittaInfo> dittaInfos = dittaInfoRepository.findAllByOrderByCodiceAsc();
        log.info("Retrieved {} 'ditta-info'", dittaInfos.size());
        return dittaInfos;
    }

    public DittaInfo getOne(Long dittaInfoId){
        log.info("Retrieving 'ditta-info' '{}'", dittaInfoId);
        DittaInfo dittaInfo = dittaInfoRepository.findById(dittaInfoId).orElseThrow(ResourceNotFoundException::new);
        log.info("Retrieved 'ditta-info' '{}'", dittaInfo);
        return dittaInfo;
    }

    @Transactional
    public DittaInfo update(DittaInfo dittaInfo){
        log.info("Updating 'ditta-info'");

        DittaInfo currentDittaInfo = dittaInfoRepository.findById(dittaInfo.getId()).orElseThrow(ResourceNotFoundException::new);
        dittaInfo.setCodice(currentDittaInfo.getCodice());
        dittaInfo.setDescrizione(currentDittaInfo.getDescrizione());
        dittaInfo.setDeletable(currentDittaInfo.getDeletable());
        dittaInfo.setDataInserimento(currentDittaInfo.getDataInserimento());
        dittaInfo.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));
        DittaInfo updatedDittaInfo = dittaInfoRepository.save(dittaInfo);

        log.info("Updated 'ditta-info' '{}'", updatedDittaInfo);
        return updatedDittaInfo;
    }

}
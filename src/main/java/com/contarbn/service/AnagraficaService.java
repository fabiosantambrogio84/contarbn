package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Anagrafica;
import com.contarbn.repository.AnagraficaRepository;
import com.contarbn.util.Utils;
import com.contarbn.util.enumeration.TipologiaAnagrafica;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnagraficaService {

    private final AnagraficaRepository anagraficaRepository;

    public List<Anagrafica> getAllByTypo(TipologiaAnagrafica tipologiaAnagrafica, Boolean active){
        log.info("Retrieving the list of 'anagrafica {}'", tipologiaAnagrafica.name());
        return anagraficaRepository.findAllByTipoAndAttivo(tipologiaAnagrafica.name(), Utils.getActiveValues(active));
    }

    public Anagrafica getById(Long idAnagrafica){
        log.info("Retrieving 'anagrafica' with id {}", idAnagrafica);
        return anagraficaRepository.findById(idAnagrafica).orElseThrow(ResourceNotFoundException::new);
    }

    public Anagrafica save(Anagrafica anagrafica){
        log.info("Saving 'anagrafica' {}", anagrafica);
        anagrafica.setAttivo(anagrafica.getAttivo() != null ? anagrafica.getAttivo() : Boolean.TRUE);
        anagrafica.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));
        if(anagrafica.getId() == null){
            anagrafica.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        } else {
            anagraficaRepository.findById(anagrafica.getId()).ifPresent(a -> anagrafica.setDataInserimento(a.getDataInserimento()));
        }
        return anagraficaRepository.save(anagrafica);
    }

    public void deleteById(Long idAnagrafica){
        log.info("Deleting 'anagrafica' with id {}", idAnagrafica);
        anagraficaRepository.deleteById(idAnagrafica);
    }

}
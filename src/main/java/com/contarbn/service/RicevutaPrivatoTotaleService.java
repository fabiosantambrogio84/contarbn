package com.contarbn.service;

import com.contarbn.model.RicevutaPrivatoTotale;
import com.contarbn.repository.RicevutaPrivatoTotaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class RicevutaPrivatoTotaleService {

    private final RicevutaPrivatoTotaleRepository ricevutaPrivatoTotaleRepository;

    public Set<RicevutaPrivatoTotale> findAll(){
        log.info("Retrieving the list of 'ricevuta privato totali'");
        Set<RicevutaPrivatoTotale> ricevutaPrivatoTotali = ricevutaPrivatoTotaleRepository.findAll();
        log.info("Retrieved {} 'ricevuta privato totali'", ricevutaPrivatoTotali.size());
        return ricevutaPrivatoTotali;
    }

    @Transactional
    public RicevutaPrivatoTotale create(RicevutaPrivatoTotale ricevutaPrivatoTotale){
        log.info("Creating 'ricevuta privato totale'");
        ricevutaPrivatoTotale.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));

        RicevutaPrivatoTotale createdRicevutaPrivatoTotale = ricevutaPrivatoTotaleRepository.save(ricevutaPrivatoTotale);
        log.info("Created 'ricevuta privato totale' '{}'", createdRicevutaPrivatoTotale);
        return createdRicevutaPrivatoTotale;
    }

    @Transactional
    public void deleteByRicevutaPrivatoId(Long ricevutaPrivatoId){
        log.info("Deleting 'ricevuta privato totali' by 'ricevuta privato' '{}'", ricevutaPrivatoId);
        ricevutaPrivatoTotaleRepository.deleteByRicevutaPrivatoId(ricevutaPrivatoId);
        log.info("Deleted 'ricevuta privato totali' by 'ricevuta privato' '{}'", ricevutaPrivatoId);
    }

}

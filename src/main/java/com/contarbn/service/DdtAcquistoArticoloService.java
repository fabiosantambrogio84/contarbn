package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Articolo;
import com.contarbn.model.DdtAcquistoArticolo;
import com.contarbn.repository.DdtAcquistoArticoloRepository;
import com.contarbn.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DdtAcquistoArticoloService {

    private final DdtAcquistoArticoloRepository ddtAcquistoArticoloRepository;

    private final ArticoloService articoloService;

    public Set<DdtAcquistoArticolo> findAll(){
        log.info("Retrieving the list of 'ddt acquisto articoli'");
        Set<DdtAcquistoArticolo> ddtAcquistoArticoli = ddtAcquistoArticoloRepository.findAll();
        log.info("Retrieved {} 'ddt acquisto articoli'", ddtAcquistoArticoli.size());
        return ddtAcquistoArticoli;
    }

    public Set<DdtAcquistoArticolo> findByDdtAcquistoId(Long idDdtAcquisto){
        log.info("Retrieving the list of 'ddt acquisto articoli' of 'ddtAcquisto' {}", idDdtAcquisto);
        Set<DdtAcquistoArticolo> ddtAcquistoArticoli = ddtAcquistoArticoloRepository.findByDdtAcquistoId(idDdtAcquisto);
        log.info("Retrieved {} 'ddt acquisto articoli'", ddtAcquistoArticoli.size());
        return ddtAcquistoArticoli;
    }

    public DdtAcquistoArticolo create(DdtAcquistoArticolo ddtAcquistoArticolo){
        log.info("Creating 'ddt acquistoarticolo'");
        ddtAcquistoArticolo.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        ddtAcquistoArticolo.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));
        ddtAcquistoArticolo.setImponibile(computeImponibile(ddtAcquistoArticolo));

        DdtAcquistoArticolo createdDdtAcquistoArticolo = ddtAcquistoArticoloRepository.save(ddtAcquistoArticolo);
        log.info("Created 'ddt articolo' '{}'", createdDdtAcquistoArticolo);
        return createdDdtAcquistoArticolo;
    }

    public DdtAcquistoArticolo update(DdtAcquistoArticolo ddtAcquistoArticolo){
        log.info("Updating 'ddt acquisto articolo'");
        DdtAcquistoArticolo ddtAcquistoArticoloCurrent = ddtAcquistoArticoloRepository.findById(ddtAcquistoArticolo.getId()).orElseThrow(ResourceNotFoundException::new);
        ddtAcquistoArticolo.setDataInserimento(ddtAcquistoArticoloCurrent.getDataInserimento());
        ddtAcquistoArticolo.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));
        ddtAcquistoArticolo.setImponibile(computeImponibile(ddtAcquistoArticolo));

        DdtAcquistoArticolo updatedDdtAcquistoArticolo = ddtAcquistoArticoloRepository.save(ddtAcquistoArticolo);
        log.info("Updated 'ddt acquisto articolo' '{}'", updatedDdtAcquistoArticolo);
        return updatedDdtAcquistoArticolo;
    }

    public void deleteByDdtAcquistoId(Long ddtAcquistoId){
        log.info("Deleting 'ddt acquisto articolo' by 'ddt' '{}'", ddtAcquistoId);
        ddtAcquistoArticoloRepository.deleteByDdtAcquistoId(ddtAcquistoId);
        log.info("Deleted 'ddt acquisto articolo' by 'ddt' '{}'", ddtAcquistoId);
    }

    public Articolo getArticolo(DdtAcquistoArticolo ddtAcquistoArticolo){
        Long articoloId = ddtAcquistoArticolo.getId().getArticoloId();
        return articoloService.getOne(articoloId);
    }

    public Set<DdtAcquistoArticolo> getByArticoloIdAndLottoAndScadenza(Long idArticolo, String lotto, Date scadenza, Timestamp dataAggiornamento){
        log.info("Retrieving 'ddt acquisto articoli' by 'idArticolo' '{}', 'lotto' '{}' and 'scadenza' '{}'", idArticolo, lotto, scadenza);
        Set<DdtAcquistoArticolo> ddtAcquistoArticoli = ddtAcquistoArticoloRepository.findByArticoloIdAndLotto(idArticolo, lotto);
        if(ddtAcquistoArticoli != null && !ddtAcquistoArticoli.isEmpty()){
            if(scadenza != null){
                ddtAcquistoArticoli = ddtAcquistoArticoli.stream()
                        .filter(daa -> (daa.getDataScadenza() != null && daa.getDataScadenza().toLocalDate().compareTo(scadenza.toLocalDate())==0)).collect(Collectors.toSet());
            }
            if(dataAggiornamento != null){
                ddtAcquistoArticoli = ddtAcquistoArticoli.stream()
                        .filter(daa -> daa.getDataAggiornamento().after(dataAggiornamento)).collect(Collectors.toSet());
            }
        }
        log.info("Retrieved '{}' 'ddt acquisto articoli'", ddtAcquistoArticoli.size());
        return ddtAcquistoArticoli;
    }

    private BigDecimal computeImponibile(DdtAcquistoArticolo ddtAcquistoArticolo){
        BigDecimal imponibile = new BigDecimal(0);

        // imponibile = (quantita*prezzo)-sconto
        Float quantita = ddtAcquistoArticolo.getQuantita();
        if(quantita == null){
            quantita = 0F;
        }
        BigDecimal prezzo = ddtAcquistoArticolo.getPrezzo();
        if(prezzo == null){
            prezzo = new BigDecimal(0);
        }
        BigDecimal sconto = ddtAcquistoArticolo.getSconto();
        if(sconto == null){
            sconto = new BigDecimal(0);
        }
        BigDecimal quantitaPerPrezzo = prezzo.multiply(BigDecimal.valueOf(quantita));
        BigDecimal scontoValue = sconto.divide(BigDecimal.valueOf(100)).multiply(quantitaPerPrezzo);

        imponibile = Utils.roundPrice(quantitaPerPrezzo.subtract(scontoValue));
        return imponibile;
    }

}

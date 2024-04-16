package com.contarbn.service;

import com.contarbn.model.Articolo;
import com.contarbn.model.DdtArticolo;
import com.contarbn.model.RicevutaPrivatoArticolo;
import com.contarbn.repository.RicevutaPrivatoArticoloRepository;
import com.contarbn.util.AccountingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RicevutaPrivatoArticoloService {

    private final RicevutaPrivatoArticoloRepository ricevutaPrivatoArticoloRepository;
    private final ArticoloService articoloService;

    public Set<RicevutaPrivatoArticolo> findAll(){
        log.info("Retrieving the list of 'ricevuta privato articoli'");
        Set<RicevutaPrivatoArticolo> ricevutaPrivatoArticoli = ricevutaPrivatoArticoloRepository.findAll();
        log.info("Retrieved {} 'ricevuta privato articoli'", ricevutaPrivatoArticoli.size());
        return ricevutaPrivatoArticoli;
    }

    public Set<RicevutaPrivatoArticolo> findByRicevutaPrivatoId(Long idRicevutaPrivato){
        log.info("Retrieving the list of 'ricevuta privato articoli' of 'ricevuta privato' {}", idRicevutaPrivato);
        Set<RicevutaPrivatoArticolo> ricevutaPrivatoArticoli = ricevutaPrivatoArticoloRepository.findByRicevutaPrivatoId(idRicevutaPrivato);
        log.info("Retrieved {} 'ricevuta privato articoli'", ricevutaPrivatoArticoli.size());
        return ricevutaPrivatoArticoli;
    }

    @Transactional
    public RicevutaPrivatoArticolo create(RicevutaPrivatoArticolo ricevutaPrivatoArticolo){
        log.info("Creating 'ricevuta privato articolo'");
        ricevutaPrivatoArticolo.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        ricevutaPrivatoArticolo.setImponibile(computeImponibile(ricevutaPrivatoArticolo));
        ricevutaPrivatoArticolo.setCosto(computeCosto(ricevutaPrivatoArticolo));
        ricevutaPrivatoArticolo.setTotale(computeTotale(ricevutaPrivatoArticolo));

        RicevutaPrivatoArticolo createdRicevutaPrivatoArticolo = ricevutaPrivatoArticoloRepository.save(ricevutaPrivatoArticolo);

        log.info("Created 'ricevuta privato articolo' '{}'", createdRicevutaPrivatoArticolo);
        return createdRicevutaPrivatoArticolo;
    }

    @Transactional
    public void deleteByRicevutaPrivatoId(Long ricevutaPrivatoId){
        log.info("Deleting 'ricevuta privato articolo' by 'ricevuta privato' '{}'", ricevutaPrivatoId);
        ricevutaPrivatoArticoloRepository.deleteByRicevutaPrivatoId(ricevutaPrivatoId);
        log.info("Deleted 'ricevuta privato articolo' by 'ricevuta privato' '{}'", ricevutaPrivatoId);
    }

    public Articolo getArticolo(RicevutaPrivatoArticolo ricevutaPrivatoArticolo){
        Long articoloId = ricevutaPrivatoArticolo.getId().getArticoloId();
        return articoloService.getOne(articoloId);
    }

    public Set<RicevutaPrivatoArticolo> getByArticoloIdAndLottoAndScadenza(Long idArticolo, String lotto, Date scadenza, Timestamp dataAggiornamento){
        log.info("Retrieving 'ricevuta privato articoli' by 'idArticolo' '{}', 'lotto' '{}' and 'scadenza' '{}'", idArticolo, lotto, scadenza);
        Set<RicevutaPrivatoArticolo> ricevutaPrivatoArticoli = ricevutaPrivatoArticoloRepository.findByArticoloIdAndLotto(idArticolo, lotto);
        if(ricevutaPrivatoArticoli != null && !ricevutaPrivatoArticoli.isEmpty()){
            if(scadenza != null){
                ricevutaPrivatoArticoli = ricevutaPrivatoArticoli.stream()
                        .filter(rpa -> (rpa.getScadenza() != null && rpa.getScadenza().toLocalDate().compareTo(scadenza.toLocalDate())==0)).collect(Collectors.toSet());
            }
            if(dataAggiornamento != null){
                ricevutaPrivatoArticoli = ricevutaPrivatoArticoli.stream()
                        .filter(rpa -> rpa.getDataAggiornamento().after(dataAggiornamento)).collect(Collectors.toSet());
            }
        }
        log.info("Retrieved '{}' 'ricevuta privato articoli'", ricevutaPrivatoArticoli.size());
        return ricevutaPrivatoArticoli;
    }
    
    private BigDecimal computeImponibile(RicevutaPrivatoArticolo ricevutaPrivatoArticolo){
        return AccountingUtils.computeImponibile(ricevutaPrivatoArticolo.getQuantita(), ricevutaPrivatoArticolo.getPrezzo(), ricevutaPrivatoArticolo.getSconto());
    }

    private BigDecimal computeCosto(RicevutaPrivatoArticolo ricevutaPrivatoArticolo){
        return AccountingUtils.computeCosto(ricevutaPrivatoArticolo.getQuantita(), ricevutaPrivatoArticolo.getId().getArticoloId(), articoloService);
    }

    private BigDecimal computeTotale(RicevutaPrivatoArticolo ricevutaPrivatoArticolo){
        return AccountingUtils.computeTotale(ricevutaPrivatoArticolo.getQuantita(), ricevutaPrivatoArticolo.getPrezzo(), ricevutaPrivatoArticolo.getSconto(), null, ricevutaPrivatoArticolo.getId().getArticoloId(), articoloService);
    }

}

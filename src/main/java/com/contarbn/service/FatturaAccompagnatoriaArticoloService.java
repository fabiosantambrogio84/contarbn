package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Articolo;
import com.contarbn.model.FatturaAccompagnatoriaArticolo;
import com.contarbn.repository.FatturaAccompagnatoriaArticoloRepository;
import com.contarbn.util.AccountingUtils;
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
public class FatturaAccompagnatoriaArticoloService {

    private final FatturaAccompagnatoriaArticoloRepository fatturaAccompagnatoriaArticoloRepository;
    private final ArticoloService articoloService;

    public Set<FatturaAccompagnatoriaArticolo> findAll(){
        log.info("Retrieving the list of 'fattura accompagnatoria articoli'");
        Set<FatturaAccompagnatoriaArticolo> fatturaAccompagnatoriaArticoli = fatturaAccompagnatoriaArticoloRepository.findAll();
        log.info("Retrieved {} 'fattura accompagnatoria articoli'", fatturaAccompagnatoriaArticoli.size());
        return fatturaAccompagnatoriaArticoli;
    }

    public Set<FatturaAccompagnatoriaArticolo> findByFatturaAccompagnatoriaId(Long idFatturaAccompagnatoria){
        log.info("Retrieving the list of 'fattura accompagnatoria articoli' of 'fattura accompagnatoria' {}", idFatturaAccompagnatoria);
        Set<FatturaAccompagnatoriaArticolo> fatturaAccompagnatoriaArticoli = fatturaAccompagnatoriaArticoloRepository.findByFatturaAccompagnatoriaId(idFatturaAccompagnatoria);
        log.info("Retrieved {} 'fattura accompagnatoria articoli'", fatturaAccompagnatoriaArticoli.size());
        return fatturaAccompagnatoriaArticoli;
    }

    public FatturaAccompagnatoriaArticolo create(FatturaAccompagnatoriaArticolo fatturaAccompagnatoriaArticolo){
        log.info("Creating 'fattura accompagnatoria articolo'");
        fatturaAccompagnatoriaArticolo.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        fatturaAccompagnatoriaArticolo.setImponibile(computeImponibile(fatturaAccompagnatoriaArticolo));
        fatturaAccompagnatoriaArticolo.setCosto(computeCosto(fatturaAccompagnatoriaArticolo));
        fatturaAccompagnatoriaArticolo.setTotale(computeTotale(fatturaAccompagnatoriaArticolo));

        FatturaAccompagnatoriaArticolo createdFatturaAccompagnatoriaArticolo = fatturaAccompagnatoriaArticoloRepository.save(fatturaAccompagnatoriaArticolo);

        log.info("Created 'fattura accompagnatoria articolo' '{}'", createdFatturaAccompagnatoriaArticolo);
        return createdFatturaAccompagnatoriaArticolo;
    }

    public FatturaAccompagnatoriaArticolo update(FatturaAccompagnatoriaArticolo fatturaAccompagnatoriaArticolo){
        log.info("Updating 'fattura accompagnatoria articolo'");
        FatturaAccompagnatoriaArticolo fatturaAccompagnatoriaArticoloCurrent = fatturaAccompagnatoriaArticoloRepository.findById(fatturaAccompagnatoriaArticolo.getId()).orElseThrow(ResourceNotFoundException::new);
        fatturaAccompagnatoriaArticolo.setDataInserimento(fatturaAccompagnatoriaArticoloCurrent.getDataInserimento());
        fatturaAccompagnatoriaArticolo.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));
        fatturaAccompagnatoriaArticolo.setImponibile(computeImponibile(fatturaAccompagnatoriaArticolo));
        fatturaAccompagnatoriaArticolo.setCosto(computeCosto(fatturaAccompagnatoriaArticolo));
        fatturaAccompagnatoriaArticolo.setTotale(computeTotale(fatturaAccompagnatoriaArticolo));

        FatturaAccompagnatoriaArticolo updatedFatturaAccompagnatoriaArticolo = fatturaAccompagnatoriaArticoloRepository.save(fatturaAccompagnatoriaArticolo);
        log.info("Updated 'fattura accompagnatoria articolo' '{}'", updatedFatturaAccompagnatoriaArticolo);
        return updatedFatturaAccompagnatoriaArticolo;
    }

    public void deleteByFatturaAccompagnatoriaId(Long fatturaAccompagnatoriaId){
        log.info("Deleting 'fattura accompagnatoria articolo' by 'fattura accompagnatoria' '{}'", fatturaAccompagnatoriaId);
        fatturaAccompagnatoriaArticoloRepository.deleteByFatturaAccompagnatoriaId(fatturaAccompagnatoriaId);
        log.info("Deleted 'fattura accompagnatoria articolo' by 'fattura accompagnatoria' '{}'", fatturaAccompagnatoriaId);
    }

    public Articolo getArticolo(FatturaAccompagnatoriaArticolo fatturaAccompagnatoriaArticolo){
        Long articoloId = fatturaAccompagnatoriaArticolo.getId().getArticoloId();
        return articoloService.getOne(articoloId);
    }

    public Set<FatturaAccompagnatoriaArticolo> getByArticoloIdAndLottoAndScadenza(Long idArticolo, String lotto, Date scadenza, Timestamp dataAggiornamento){
        log.info("Retrieving 'fattura accompagnatoria articoli' by 'idArticolo' '{}', 'lotto' '{}' and 'scadenza' '{}'", idArticolo, lotto, scadenza);
        Set<FatturaAccompagnatoriaArticolo> fatturaAccompagnatoriaArticoli = fatturaAccompagnatoriaArticoloRepository.findByArticoloIdAndLotto(idArticolo, lotto);
        if(fatturaAccompagnatoriaArticoli != null && !fatturaAccompagnatoriaArticoli.isEmpty()){
            if(scadenza != null){
                fatturaAccompagnatoriaArticoli = fatturaAccompagnatoriaArticoli.stream()
                        .filter(faa -> (faa.getScadenza() != null && faa.getScadenza().toLocalDate().compareTo(scadenza.toLocalDate())==0)).collect(Collectors.toSet());
            }
            if(dataAggiornamento != null){
                fatturaAccompagnatoriaArticoli = fatturaAccompagnatoriaArticoli.stream()
                        .filter(faa -> faa.getDataAggiornamento().after(dataAggiornamento)).collect(Collectors.toSet());
            }
        }
        log.info("Retrieved '{}' 'fattura accompagnatoria articoli'", fatturaAccompagnatoriaArticoli.size());
        return fatturaAccompagnatoriaArticoli;
    }

    private BigDecimal computeImponibile(FatturaAccompagnatoriaArticolo fatturaAccompagnatoriaArticolo){

        return AccountingUtils.computeImponibile(fatturaAccompagnatoriaArticolo.getQuantita(), fatturaAccompagnatoriaArticolo.getPrezzo(), fatturaAccompagnatoriaArticolo.getSconto());
    }

    private BigDecimal computeCosto(FatturaAccompagnatoriaArticolo fatturaAccompagnatoriaArticolo){

        return AccountingUtils.computeCosto(fatturaAccompagnatoriaArticolo.getQuantita(), fatturaAccompagnatoriaArticolo.getId().getArticoloId(), articoloService);
    }

    private BigDecimal computeTotale(FatturaAccompagnatoriaArticolo fatturaAccompagnatoriaArticolo){
        return AccountingUtils.computeTotale(fatturaAccompagnatoriaArticolo.getQuantita(), fatturaAccompagnatoriaArticolo.getPrezzo(), fatturaAccompagnatoriaArticolo.getSconto(), null, fatturaAccompagnatoriaArticolo.getId().getArticoloId(), articoloService);
    }

}

package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Articolo;
import com.contarbn.model.RicevutaPrivatoArticolo;
import com.contarbn.repository.RicevutaPrivatoArticoloRepository;
import com.contarbn.util.AccountingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Set;

@Service
public class RicevutaPrivatoArticoloService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RicevutaPrivatoArticoloService.class);

    //private static final String CONTEXT_CREATE_RICEVUTA_PRIVATO = "create_ricevuta_privato";
    //private static final String CONTEXT_DELETE_RICEVAUTA_PRIVATO = "delete_ricevuta_privato";

    private final RicevutaPrivatoArticoloRepository ricevutaPrivatoArticoloRepository;
    private final ArticoloService articoloService;
    //private final RicevutaPrivatoArticoloOrdineClienteRepository ricevutaPrivatoArticoloOrdineClienteRepository;
    //private final OrdineClienteService ordineClienteService;

    @Autowired
    public RicevutaPrivatoArticoloService(final RicevutaPrivatoArticoloRepository ricevutaPrivatoArticoloRepository,
                                          final ArticoloService articoloService){
                                          //final RicevutaPrivatoArticoloOrdineClienteRepository ricevutaPrivatoArticoloOrdineClienteRepository,
                                          //final OrdineClienteService ordineClienteService){
        this.ricevutaPrivatoArticoloRepository = ricevutaPrivatoArticoloRepository;
        this.articoloService = articoloService;
        //this.ricevutaPrivatoArticoloOrdineClienteRepository = ricevutaPrivatoArticoloOrdineClienteRepository;
        //this.ordineClienteService = ordineClienteService;
    }

    public Set<RicevutaPrivatoArticolo> findAll(){
        LOGGER.info("Retrieving the list of 'ricevuta privato articoli'");
        Set<RicevutaPrivatoArticolo> ricevutaPrivatoArticoli = ricevutaPrivatoArticoloRepository.findAll();
        LOGGER.info("Retrieved {} 'ricevuta privato articoli'", ricevutaPrivatoArticoli.size());
        return ricevutaPrivatoArticoli;
    }

    public Set<RicevutaPrivatoArticolo> findByRicevutaPrivatoId(Long idRicevutaPrivato){
        LOGGER.info("Retrieving the list of 'ricevuta privato articoli' of 'ricevuta privato' {}", idRicevutaPrivato);
        Set<RicevutaPrivatoArticolo> ricevutaPrivatoArticoli = ricevutaPrivatoArticoloRepository.findByRicevutaPrivatoId(idRicevutaPrivato);
        LOGGER.info("Retrieved {} 'ricevuta privato articoli'", ricevutaPrivatoArticoli.size());
        return ricevutaPrivatoArticoli;
    }

    public RicevutaPrivatoArticolo create(RicevutaPrivatoArticolo ricevutaPrivatoArticolo){
        LOGGER.info("Creating 'ricevuta privato articolo'");
        ricevutaPrivatoArticolo.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        ricevutaPrivatoArticolo.setImponibile(computeImponibile(ricevutaPrivatoArticolo));
        ricevutaPrivatoArticolo.setCosto(computeCosto(ricevutaPrivatoArticolo));
        ricevutaPrivatoArticolo.setTotale(computeTotale(ricevutaPrivatoArticolo));

        RicevutaPrivatoArticolo createdRicevutaPrivatoArticolo = ricevutaPrivatoArticoloRepository.save(ricevutaPrivatoArticolo);

        LOGGER.info("Created 'ricevuta privato articolo' '{}'", createdRicevutaPrivatoArticolo);
        return createdRicevutaPrivatoArticolo;
    }

    public RicevutaPrivatoArticolo update(RicevutaPrivatoArticolo ricevutaPrivatoArticolo){
        LOGGER.info("Updating 'ricevuta privato articolo'");
        RicevutaPrivatoArticolo ricevutaPrivatoArticoloCurrent = ricevutaPrivatoArticoloRepository.findById(ricevutaPrivatoArticolo.getId()).orElseThrow(ResourceNotFoundException::new);
        ricevutaPrivatoArticolo.setDataInserimento(ricevutaPrivatoArticoloCurrent.getDataInserimento());
        ricevutaPrivatoArticolo.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));
        ricevutaPrivatoArticolo.setImponibile(computeImponibile(ricevutaPrivatoArticolo));
        ricevutaPrivatoArticolo.setCosto(computeCosto(ricevutaPrivatoArticolo));
        ricevutaPrivatoArticolo.setTotale(computeTotale(ricevutaPrivatoArticolo));

        RicevutaPrivatoArticolo updatedRicevutaPrivatoArticolo = ricevutaPrivatoArticoloRepository.save(ricevutaPrivatoArticolo);
        LOGGER.info("Updated 'ricevuta privato articolo' '{}'", updatedRicevutaPrivatoArticolo);
        return updatedRicevutaPrivatoArticolo;
    }

    public void deleteByRicevutaPrivatoId(Long ricevutaPrivatoId){
        LOGGER.info("Deleting 'ricevuta privato articolo' by 'ricevuta privato' '{}'", ricevutaPrivatoId);
        ricevutaPrivatoArticoloRepository.deleteByRicevutaPrivatoId(ricevutaPrivatoId);
        LOGGER.info("Deleted 'ricevuta privato articolo' by 'ricevuta privato' '{}'", ricevutaPrivatoId);
    }

    public Articolo getArticolo(RicevutaPrivatoArticolo ricevutaPrivatoArticolo){
        Long articoloId = ricevutaPrivatoArticolo.getId().getArticoloId();
        return articoloService.getOne(articoloId);
    }

    /*
    public Set<RicevutaPrivatoArticolo> getByArticoloIdAndLottoAndScadenza(Long idArticolo, String lotto, Date scadenza){
        LOGGER.info("Retrieving 'ricevuta privato articoli' by 'idArticolo' '{}', 'lotto' '{}' and 'scadenza' '{}'", idArticolo, lotto, scadenza);
        Set<RicevutaPrivatoArticolo> ricevutaPrivatoArticoli = ricevutaPrivatoArticoloRepository.findByArticoloIdAndLotto(idArticolo, lotto);
        if(ricevutaPrivatoArticoli != null && !ricevutaPrivatoArticoli.isEmpty()){
            if(scadenza != null){
                ricevutaPrivatoArticoli = ricevutaPrivatoArticoli.stream()
                        .filter(faa -> (faa.getScadenza() != null && faa.getScadenza().toLocalDate().compareTo(scadenza.toLocalDate())==0)).collect(Collectors.toSet());
            }
        }
        LOGGER.info("Retrieved '{}' 'ricevuta privato articoli'", ricevutaPrivatoArticoli.size());
        return ricevutaPrivatoArticoli;
    }

    private Integer computeOrdineClienteArticoloNewPezziDaEvadere(String context, Integer pezzi, Integer pezziDaEvadere, Integer pezziOrdinati, Map<RicevutaPrivatoArticoloKey, Integer> ricevutaPrivatoArticoliPezziRemaining, RicevutaPrivatoArticoloKey ricevutaPrivatoArticoloKey){
        LOGGER.info("Computing 'newPezziDaEvadere' for context {}, pezzi {}, pezziDaEvadere {}, pezziOrdinati {}", context, pezzi, pezziDaEvadere, pezziOrdinati);

        Integer newNumeroPezziDaEvadere = null;
        if(context.equals(CONTEXT_CREATE_RICEVUTA_PRIVATO)){
            newNumeroPezziDaEvadere = pezziDaEvadere - pezzi;
            if(newNumeroPezziDaEvadere < 0){
                LOGGER.info("Context {}: pezzi da evadere {} - pezzi {} less than 0", context, pezziDaEvadere, pezzi);
                newNumeroPezziDaEvadere = 0;
                ricevutaPrivatoArticoliPezziRemaining.putIfAbsent(ricevutaPrivatoArticoloKey, Math.abs(pezziDaEvadere - pezzi));
            }
        } else if(context.equals(CONTEXT_DELETE_RICEVAUTA_PRIVATO)){
            newNumeroPezziDaEvadere = pezziDaEvadere + pezzi;
            if(newNumeroPezziDaEvadere > pezziOrdinati){
                newNumeroPezziDaEvadere = pezziOrdinati;
                LOGGER.info("Context {}: pezzi da evadere {} + pezzi {} greater than pezzi ordinati {}", context, pezziDaEvadere, pezzi, pezziOrdinati);
                ricevutaPrivatoArticoliPezziRemaining.putIfAbsent(ricevutaPrivatoArticoloKey, Math.abs(pezziOrdinati-pezzi));
            }
        }
        LOGGER.info("Context {}: pezzi: {}, pezzi da evadere {} pezzi ordinati {}, nuovi pezzi da evadere {}", context, pezzi, pezziDaEvadere, pezziOrdinati, newNumeroPezziDaEvadere);
        return newNumeroPezziDaEvadere;
    }
    */

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

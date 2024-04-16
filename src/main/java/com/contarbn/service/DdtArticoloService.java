package com.contarbn.service;

import com.contarbn.model.*;
import com.contarbn.repository.DdtArticoloOrdineClienteRepository;
import com.contarbn.repository.DdtArticoloRepository;
import com.contarbn.util.AccountingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DdtArticoloService {

    private static final String CONTEXT_CREATE_DDT = "create_ddt";
    private static final String CONTEXT_DELETE_DDT = "delete_ddt";

    private final DdtArticoloRepository ddtArticoloRepository;
    private final ArticoloService articoloService;
    private final DdtArticoloOrdineClienteRepository ddtArticoloOrdineClienteRepository;
    private final OrdineClienteService ordineClienteService;

    public Set<DdtArticolo> findAll(){
        log.info("Retrieving the list of 'ddt articoli'");
        Set<DdtArticolo> ddtArticoli = ddtArticoloRepository.findAll();
        log.info("Retrieved {} 'ddt articoli'", ddtArticoli.size());
        return ddtArticoli;
    }

    public Set<DdtArticolo> findByDdtId(Long idDdt){
        log.info("Retrieving the list of 'ddt articoli' of 'ddt' {}", idDdt);
        Set<DdtArticolo> ddtArticoli = ddtArticoloRepository.findByDdtId(idDdt);
        log.info("Retrieved {} 'ddt articoli'", ddtArticoli.size());
        return ddtArticoli;
    }

    @Transactional
    public DdtArticolo create(DdtArticolo ddtArticolo){
        log.info("Creating 'ddt articolo'");
        ddtArticolo.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        ddtArticolo.setImponibile(computeImponibile(ddtArticolo));
        ddtArticolo.setCosto(computeCosto(ddtArticolo));
        ddtArticolo.setTotale(computeTotale(ddtArticolo));

        //List<Long> idOrdiniClienti = ddtArticolo.getIdOrdiniClienti();

        DdtArticolo createdDdtArticolo = ddtArticoloRepository.save(ddtArticolo);

        /*if(idOrdiniClienti != null && !idOrdiniClienti.isEmpty()){
            log.info("Creating 'ddt articoli ordini clienti'");
            for (Long idOrdineCliente: idOrdiniClienti) {
                DdtArticoloOrdineClienteKey ddtArticoloOrdineClienteKey = new DdtArticoloOrdineClienteKey();
                ddtArticoloOrdineClienteKey.setDdtId(createdDdtArticolo.getId().getDdtId());
                ddtArticoloOrdineClienteKey.setArticoloId(createdDdtArticolo.getId().getArticoloId());
                ddtArticoloOrdineClienteKey.setUuid(createdDdtArticolo.getId().getUuid());
                ddtArticoloOrdineClienteKey.setOrdineClienteId(idOrdineCliente);

                DdtArticoloOrdineCliente ddtArticoloOrdineCliente = new DdtArticoloOrdineCliente();
                ddtArticoloOrdineCliente.setId(ddtArticoloOrdineClienteKey);
                ddtArticoloOrdineCliente.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));

                ddtArticoloOrdineClienteRepository.save(ddtArticoloOrdineCliente);
            }
            log.info("Created 'ddt articoli ordini clienti'");
        }*/

        log.info("Created 'ddt articolo' '{}'", createdDdtArticolo);
        return createdDdtArticolo;
    }

    @Transactional
    public void deleteByDdtId(Long ddtId){
        log.info("Deleting 'ddt articolo' by 'ddt' '{}'", ddtId);
        ddtArticoloRepository.deleteByDdtId(ddtId);
        ddtArticoloOrdineClienteRepository.deleteByIdDdtId(ddtId);
        log.info("Deleted 'ddt articolo' by 'ddt' '{}'", ddtId);
    }

    public Articolo getArticolo(DdtArticolo ddtArticolo){
        Long articoloId = ddtArticolo.getId().getArticoloId();
        return articoloService.getOne(articoloId);
    }

    public Set<DdtArticolo> getByArticoloIdAndLottoAndScadenza(Long idArticolo, String lotto, Date scadenza, Timestamp dataAggiornamento){
        log.info("Retrieving 'ddt articoli' by 'idArticolo' '{}', 'lotto' '{}' and 'scadenza' '{}'", idArticolo, lotto, scadenza);
        Set<DdtArticolo> ddtArticoli = ddtArticoloRepository.findByArticoloIdAndLotto(idArticolo, lotto);
        if(ddtArticoli != null && !ddtArticoli.isEmpty()){
            if(scadenza != null){
                ddtArticoli = ddtArticoli.stream()
                        .filter(da -> (da.getScadenza() != null && da.getScadenza().toLocalDate().compareTo(scadenza.toLocalDate())==0)).collect(Collectors.toSet());
            }
            if(dataAggiornamento != null){
                ddtArticoli = ddtArticoli.stream()
                        .filter(da -> da.getDataAggiornamento().after(dataAggiornamento)).collect(Collectors.toSet());
            }
        }
        log.info("Retrieved '{}' 'ddt articoli'", ddtArticoli.size());
        return ddtArticoli;
    }

    @Transactional
    public void updateOrdineClienteFromDeleteDdt(Long idDdt){
        updateOrdineCliente(idDdt, CONTEXT_DELETE_DDT);
    }

    private void updateOrdineCliente(Long idDdt, String context){
        log.info("Updating 'numeroPezziDaEvadere' of 'OrdiniClientiArticoli' related to ddt '{}'", idDdt);

        Set<Long> idOrdiniClienti = new HashSet<>();
        Map<DdtArticoloKey, Integer> ddtArticoliPezziRemaining = new HashMap<>();

        // retrieve the 'DdtArticoloOrdineCliente' of the ddt
        List<DdtArticoloOrdineCliente> ddtArticoliOrdiniClienti = ddtArticoloOrdineClienteRepository.findAllByIdDdtId(idDdt);
        if(ddtArticoliOrdiniClienti != null && !ddtArticoliOrdiniClienti.isEmpty()){
            for(DdtArticoloOrdineCliente ddtArticoloOrdineCliente : ddtArticoliOrdiniClienti){
                log.info("'DdtArticoloOrdineCliente' {}", ddtArticoloOrdineCliente);

                // add the id of the 'OrdineCliente' to the set of ordini-clienti to compute stato
                Long idOrdineCliente = ddtArticoloOrdineCliente.getId().getOrdineClienteId();
                idOrdiniClienti.add(idOrdineCliente);

                // create 'DdtArticoloKey'
                DdtArticoloKey ddtArticoloKey = new DdtArticoloKey();
                ddtArticoloKey.setDdtId(ddtArticoloOrdineCliente.getId().getDdtId());
                ddtArticoloKey.setArticoloId(ddtArticoloOrdineCliente.getId().getArticoloId());
                ddtArticoloKey.setUuid(ddtArticoloOrdineCliente.getId().getUuid());

                // retrieve the 'DdtArticolo'
                DdtArticolo ddtArticolo = null;
                Optional<DdtArticolo> optionalDdtArticolo = ddtArticoloRepository.findById(ddtArticoloKey);
                if(optionalDdtArticolo.isPresent()){
                    ddtArticolo = optionalDdtArticolo.get();
                    log.info("Retrieved 'ddtArticolo' {}", ddtArticolo);
                } else {
                    String message = "Unable to retrieve 'DdtArticolo' from key '"+ddtArticoloKey+"'";
                    log.error(message);
                    throw new RuntimeException(message);
                }

                // create 'OrdineClienteArticoloKey'
                OrdineClienteArticoloKey ordineClienteArticoloKey = new OrdineClienteArticoloKey();
                ordineClienteArticoloKey.setOrdineClienteId(idOrdineCliente);
                ordineClienteArticoloKey.setArticoloId(ddtArticoloOrdineCliente.getId().getArticoloId());

                // retrieve the 'OrdineClienteArticolo'
                OrdineClienteArticolo ordineClienteArticolo = null;
                try{
                    ordineClienteArticolo = ordineClienteService.getOrdineClienteArticolo(ordineClienteArticoloKey);
                    log.info("Retrieved 'ordineClienteArticolo' {}", ordineClienteArticolo);
                } catch(Exception e){
                    log.error("Unable to retrieve 'OrdineClienteArticolo' from key '{}'", ordineClienteArticoloKey);
                    throw e;
                }

                Integer numeroPezzi = ddtArticoliPezziRemaining.getOrDefault(ddtArticoloKey, ddtArticolo.getNumeroPezzi());
                Integer numeroPezziDaEvadere = ordineClienteArticolo.getNumeroPezziDaEvadere();
                Integer newNumeroPezziDaEvadere = computeOrdineClienteArticoloNewPezziDaEvadere(context, numeroPezzi, numeroPezziDaEvadere, ordineClienteArticolo.getNumeroPezziOrdinati(), ddtArticoliPezziRemaining, ddtArticoloKey);

                log.info("Updating 'OrdineClienteArticolo' with id '{}' setting 'numeroPezziDaEvadere' equals to {}", ordineClienteArticolo.getId(), newNumeroPezziDaEvadere);
                ordineClienteArticolo.setNumeroPezziDaEvadere(newNumeroPezziDaEvadere);
                ordineClienteService.saveOrdineClienteArticolo(ordineClienteArticolo);
                log.info("Updated 'OrdineClienteArticolo' with id '{}' setting 'numeroPezziDaEvadere' equals to {}", ordineClienteArticolo.getId(), newNumeroPezziDaEvadere);
            }

        } else {
            log.info("No 'DdtArticoloOrdineCliente' rows found for ddt '{}'", idDdt);
        }
        log.info("Updated 'numeroPezziDaEvadere' of 'OrdiniClientiArticoli' related to ddt '{}'", idDdt);

        // compute stato for all 'OrdiniClienti'
        if(idOrdiniClienti != null && !idOrdiniClienti.isEmpty()){
            for(Long idOrdineCliente : idOrdiniClienti){
                ordineClienteService.computeStatoOrdineCliente(idOrdineCliente);
            }
        }
    }

    private Integer computeOrdineClienteArticoloNewPezziDaEvadere(String context, Integer pezzi, Integer pezziDaEvadere, Integer pezziOrdinati, Map<DdtArticoloKey, Integer> ddtArticoliPezziRemaining, DdtArticoloKey ddtArticoloKey){
        log.info("Computing 'newPezziDaEvadere' for context {}, pezzi {}, pezziDaEvadere {}, pezziOrdinati {}", context, pezzi, pezziDaEvadere, pezziOrdinati);

        Integer newNumeroPezziDaEvadere = null;
        if(context.equals(CONTEXT_CREATE_DDT)){
            newNumeroPezziDaEvadere = pezziDaEvadere - pezzi;
            if(newNumeroPezziDaEvadere < 0){
                log.info("Context {}: pezzi da evadere {} - pezzi {} less than 0", context, pezziDaEvadere, pezzi);
                newNumeroPezziDaEvadere = 0;
                ddtArticoliPezziRemaining.putIfAbsent(ddtArticoloKey, Math.abs(pezziDaEvadere - pezzi));
            }
        } else if(context.equals(CONTEXT_DELETE_DDT)){
            newNumeroPezziDaEvadere = pezziDaEvadere + pezzi;
            if(newNumeroPezziDaEvadere > pezziOrdinati){
                newNumeroPezziDaEvadere = pezziOrdinati;
                log.info("Context {}: pezzi da evadere {} + pezzi {} greater than pezzi ordinati {}", context, pezziDaEvadere, pezzi, pezziOrdinati);
                ddtArticoliPezziRemaining.putIfAbsent(ddtArticoloKey, Math.abs(pezziOrdinati-pezzi));
            }
        }
        log.info("Context {}: pezzi: {}, pezzi da evadere {} pezzi ordinati {}, nuovi pezzi da evadere {}", context, pezzi, pezziDaEvadere, pezziOrdinati, newNumeroPezziDaEvadere);
        return newNumeroPezziDaEvadere;
    }

    private BigDecimal computeImponibile(DdtArticolo ddtArticolo){

        return AccountingUtils.computeImponibile(ddtArticolo.getQuantita(), ddtArticolo.getPrezzo(), ddtArticolo.getSconto());
    }

    private BigDecimal computeCosto(DdtArticolo ddtArticolo){

        return AccountingUtils.computeCosto(ddtArticolo.getQuantita(), ddtArticolo.getId().getArticoloId(), articoloService);
    }

    private BigDecimal computeTotale(DdtArticolo ddtArticolo){
        return AccountingUtils.computeTotale(ddtArticolo.getQuantita(), ddtArticolo.getPrezzo(), ddtArticolo.getSconto(), null, ddtArticolo.getId().getArticoloId(), articoloService);
    }

}

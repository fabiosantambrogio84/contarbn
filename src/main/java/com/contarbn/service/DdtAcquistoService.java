package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.*;
import com.contarbn.model.beans.DdtAcquistoRicercaLotto;
import com.contarbn.repository.DdtAcquistoRepository;
import com.contarbn.util.Utils;
import com.contarbn.util.enumeration.Operation;
import com.contarbn.util.enumeration.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DdtAcquistoService {

    private final DdtAcquistoRepository ddtAcquistoRepository;
    private final DdtAcquistoArticoloService ddtAcquistoArticoloService;
    private final DdtAcquistoIngredienteService ddtAcquistoIngredienteService;
    private final GiacenzaArticoloService giacenzaArticoloService;
    private final GiacenzaIngredienteService giacenzaIngredienteService;
    private final StatoDdtService statoDdtService;

    public Set<DdtAcquisto> getAll(){
        log.info("Retrieving the list of 'ddts acquisto'");
        Set<DdtAcquisto> ddtsAcquisto = ddtAcquistoRepository.findAllByOrderByNumeroDesc();
        log.info("Retrieved {} 'ddts acquisto'", ddtsAcquisto.size());
        return ddtsAcquisto;
    }

    public Set<DdtAcquistoRicercaLotto> getAllByLotto(String lotto){
        log.info("Retrieving the list of 'ddts acquisto' filtered by 'lotto' '{}'", lotto);
        Set<DdtAcquistoRicercaLotto> ddtsAcquisto = ddtAcquistoRepository.findAllByLotto(lotto);
        log.info("Retrieved {} 'ddts acquisto'", ddtsAcquisto.size());
        return ddtsAcquisto;
    }

    public DdtAcquisto getOne(Long ddtAcquistoId){
        log.info("Retrieving 'ddt acquisto' '{}'", ddtAcquistoId);
        DdtAcquisto ddtAcquisto = ddtAcquistoRepository.findById(ddtAcquistoId).orElseThrow(ResourceNotFoundException::new);
        log.info("Retrieved 'ddt acquisto' '{}'", ddtAcquisto);
        return ddtAcquisto;
    }

    @Transactional
    public DdtAcquisto create(DdtAcquisto ddtAcquisto){
        log.info("Creating 'ddt acquisto'");

        if(ddtAcquisto.getNumeroColli() == null){
            ddtAcquisto.setNumeroColli(1);
        }
        ddtAcquisto.setStatoDdt(statoDdtService.getDaPagare());
        ddtAcquisto.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));

        DdtAcquisto createdDdtAcquisto = ddtAcquistoRepository.save(ddtAcquisto);

        createdDdtAcquisto.getDdtAcquistoArticoli().forEach(daa -> {
            daa.getId().setDdtAcquistoId(createdDdtAcquisto.getId());
            daa.getId().setUuid(UUID.randomUUID().toString());
            ddtAcquistoArticoloService.create(daa);

            // compute 'giacenza articolo'
            giacenzaArticoloService.computeGiacenza(daa.getId().getArticoloId(), daa.getLotto(), daa.getDataScadenza());
        });

        createdDdtAcquisto.getDdtAcquistoIngredienti().forEach(dai -> {
            dai.getId().setDdtAcquistoId(createdDdtAcquisto.getId());
            dai.getId().setUuid(UUID.randomUUID().toString());
            ddtAcquistoIngredienteService.create(dai);

            // compute 'giacenza ingrediente'
            giacenzaIngredienteService.computeGiacenza(dai.getId().getIngredienteId(), dai.getLotto(), dai.getDataScadenza(), dai.getQuantita(), Resource.DDT_ACQUISTO);
        });

        computeTotali(createdDdtAcquisto, createdDdtAcquisto.getDdtAcquistoArticoli(), createdDdtAcquisto.getDdtAcquistoIngredienti());

        ddtAcquistoRepository.save(createdDdtAcquisto);

        log.info("Created 'ddt acquisto' '{}'", createdDdtAcquisto);
        return createdDdtAcquisto;
    }

    @Transactional
    public DdtAcquisto update(DdtAcquisto ddtAcquisto){
        log.info("Updating 'ddt acquisto'");

        Boolean modificaGiacenze = ddtAcquisto.getModificaGiacenze();

        Set<DdtAcquistoArticolo> ddtAcquistoArticoli = ddtAcquisto.getDdtAcquistoArticoli();
        ddtAcquisto.setDdtAcquistoArticoli(new HashSet<>());
        ddtAcquistoArticoloService.deleteByDdtAcquistoId(ddtAcquisto.getId());

        Set<DdtAcquistoIngrediente> ddtAcquistoIngredienti = ddtAcquisto.getDdtAcquistoIngredienti();
        ddtAcquisto.setDdtAcquistoIngredienti(new HashSet<>());
        ddtAcquistoIngredienteService.deleteByDdtAcquistoId(ddtAcquisto.getId());

        DdtAcquisto ddtAcquistoCurrent = ddtAcquistoRepository.findById(ddtAcquisto.getId()).orElseThrow(ResourceNotFoundException::new);
        ddtAcquisto.setStatoDdt(ddtAcquistoCurrent.getStatoDdt());
        ddtAcquisto.setDataInserimento(ddtAcquistoCurrent.getDataInserimento());
        ddtAcquisto.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));

        DdtAcquisto updatedDdtAcquisto = ddtAcquistoRepository.save(ddtAcquisto);
        ddtAcquistoArticoli.forEach(daa -> {
            daa.getId().setDdtAcquistoId(updatedDdtAcquisto.getId());
            daa.getId().setUuid(UUID.randomUUID().toString());
            ddtAcquistoArticoloService.create(daa);

            if(modificaGiacenze != null && modificaGiacenze.equals(Boolean.TRUE)){
                // compute 'giacenza articolo'
                giacenzaArticoloService.computeGiacenza(daa.getId().getArticoloId(), daa.getLotto(), daa.getDataScadenza());
            }
        });
        ddtAcquistoIngredienti.forEach(dai -> {
            dai.getId().setDdtAcquistoId(updatedDdtAcquisto.getId());
            dai.getId().setUuid(UUID.randomUUID().toString());
            ddtAcquistoIngredienteService.create(dai);

            if(modificaGiacenze != null && modificaGiacenze.equals(Boolean.TRUE)){
                // compute 'giacenza ingrediente'
                giacenzaIngredienteService.computeGiacenza(dai.getId().getIngredienteId(), dai.getLotto(), dai.getDataScadenza(), dai.getQuantita(), Resource.DDT_ACQUISTO);
            }
        });

        computeTotali(updatedDdtAcquisto, ddtAcquistoArticoli, ddtAcquistoIngredienti);

        ddtAcquistoRepository.save(updatedDdtAcquisto);
        log.info("Updated 'ddt acquisto' '{}'", updatedDdtAcquisto);
        return updatedDdtAcquisto;
    }

    @Transactional
    public DdtAcquisto patch(Map<String,Object> patchDdtAcquisto){
        log.info("Patching 'ddt acquisto'");

        Long id = Long.valueOf((Integer) patchDdtAcquisto.get("id"));
        DdtAcquisto ddtAcquisto = ddtAcquistoRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        patchDdtAcquisto.forEach((key, value) -> {
            if(key.equals("id")){
                ddtAcquisto.setId(Long.valueOf((Integer)value));
            } else if(key.equals("fatturato")){
                if(value != null){
                    ddtAcquisto.setFatturato((Boolean)value);
                }
            }
        });
        DdtAcquisto patchedDdtAcquisto = ddtAcquistoRepository.save(ddtAcquisto);

        log.info("Patched 'ddt acquisto' '{}'", patchedDdtAcquisto);
        return patchedDdtAcquisto;
    }

    @Transactional
    public void delete(Long ddtAcquistoId, Boolean modificaGiacenze){
        log.info("Deleting 'ddt acquisto' '{}' ('modificaGiacenze={}')", ddtAcquistoId, modificaGiacenze);
        
        DdtAcquisto ddtAcquisto = getOne(ddtAcquistoId);
        
        Set<DdtAcquistoArticolo> ddtAcquistoArticoli = ddtAcquistoArticoloService.findByDdtAcquistoId(ddtAcquistoId);
        Set<DdtAcquistoIngrediente> ddtAcquistoIngredienti = ddtAcquistoIngredienteService.findByDdtAcquistoId(ddtAcquistoId);

        ddtAcquistoArticoloService.deleteByDdtAcquistoId(ddtAcquistoId);
        ddtAcquistoIngredienteService.deleteByDdtAcquistoId(ddtAcquistoId);
        ddtAcquistoRepository.deleteById(ddtAcquistoId);

        for (DdtAcquistoArticolo ddtAcquistoArticolo:ddtAcquistoArticoli) {

            giacenzaArticoloService.createMovimentazioneManualeArticolo(ddtAcquistoArticolo.getId().getArticoloId(), ddtAcquistoArticolo.getLotto(), ddtAcquistoArticolo.getDataScadenza(), ddtAcquistoArticolo.getNumeroPezzi(), ddtAcquistoArticolo.getQuantita(), Operation.DELETE, Resource.DDT_ACQUISTO, ddtAcquisto.getId(), ddtAcquisto.getNumero(), null, (ddtAcquisto.getFornitore() != null ? ddtAcquisto.getFornitore().getRagioneSociale() : null));

            if(modificaGiacenze.equals(Boolean.TRUE)){
                giacenzaArticoloService.computeGiacenza(ddtAcquistoArticolo.getId().getArticoloId(), ddtAcquistoArticolo.getLotto(), ddtAcquistoArticolo.getDataScadenza());
            }
        }

        for (DdtAcquistoIngrediente ddtAcquistoIngrediente:ddtAcquistoIngredienti) {
            giacenzaIngredienteService.computeGiacenza(ddtAcquistoIngrediente.getId().getIngredienteId(), ddtAcquistoIngrediente.getLotto(), ddtAcquistoIngrediente.getDataScadenza(), ddtAcquistoIngrediente.getQuantita(), Resource.DDT_ACQUISTO);
        }

        log.info("Deleted 'ddt acquisto' '{}'", ddtAcquistoId);
    }

    private void computeTotali(DdtAcquisto ddtAcquisto, Set<DdtAcquistoArticolo> ddtAcquistoArticoli, Set<DdtAcquistoIngrediente> ddtAcquistoIngredienti){
        // create and populate map for DdtAcquistoArticolo
        Map<AliquotaIva, Set<DdtAcquistoArticolo>> ivaDdtArticoliMap = new HashMap<>();
        ddtAcquistoArticoli.forEach(da -> {
            Articolo articolo = ddtAcquistoArticoloService.getArticolo(da);
            AliquotaIva iva = articolo.getAliquotaIva();
            Set<DdtAcquistoArticolo> ddtArticoliByIva;
            if(ivaDdtArticoliMap.containsKey(iva)){
                ddtArticoliByIva = ivaDdtArticoliMap.get(iva);
            } else {
                ddtArticoliByIva = new HashSet<>();
            }
            ddtArticoliByIva.add(da);
            ivaDdtArticoliMap.put(iva, ddtArticoliByIva);
        });

        // create and populate map for DdtAcquistoIngrediente
        Map<AliquotaIva, Set<DdtAcquistoIngrediente>> ivaDdtIngredientiMap = new HashMap<>();
        ddtAcquistoIngredienti.forEach(dai -> {
            Ingrediente ingrediente = ddtAcquistoIngredienteService.getIngrediente(dai);
            AliquotaIva iva = ingrediente.getAliquotaIva();
            Set<DdtAcquistoIngrediente> ddtIngredientiByIva;
            if(ivaDdtIngredientiMap.containsKey(iva)){
                ddtIngredientiByIva = ivaDdtIngredientiMap.get(iva);
            } else {
                ddtIngredientiByIva = new HashSet<>();
            }
            ddtIngredientiByIva.add(dai);
            ivaDdtIngredientiMap.put(iva, ddtIngredientiByIva);
        });

        BigDecimal totaleImponibile = new BigDecimal(0);
        BigDecimal totaleIva = new BigDecimal(0);
        BigDecimal totale = new BigDecimal(0);

        // compute 'totaleImponibile', 'totaleIva' and 'totale'
        for (Map.Entry<AliquotaIva, Set<DdtAcquistoArticolo>> entry : ivaDdtArticoliMap.entrySet()) {
            BigDecimal iva = entry.getKey().getValore();
            BigDecimal totaleByIva = new BigDecimal(0);
            Set<DdtAcquistoArticolo> ddtAcquistoArticoliByIva = entry.getValue();
            for(DdtAcquistoArticolo ddtAcquistoArticolo: ddtAcquistoArticoliByIva){
                totaleImponibile = totaleImponibile.add(ddtAcquistoArticolo.getImponibile());

                BigDecimal partialIva = ddtAcquistoArticolo.getImponibile().multiply(iva.divide(new BigDecimal(100), RoundingMode.HALF_UP));
                totaleIva = totaleIva.add(partialIva);

                totaleByIva = totaleByIva.add(ddtAcquistoArticolo.getImponibile());
            }
            totale = totale.add(totaleByIva.add(totaleByIva.multiply(iva.divide(new BigDecimal(100), RoundingMode.HALF_UP))));
        }
        for (Map.Entry<AliquotaIva, Set<DdtAcquistoIngrediente>> entry : ivaDdtIngredientiMap.entrySet()) {
            BigDecimal iva = entry.getKey().getValore();
            BigDecimal totaleByIva = new BigDecimal(0);
            Set<DdtAcquistoIngrediente> ddtAcquistoingredientiByIva = entry.getValue();
            for(DdtAcquistoIngrediente DdtAcquistoIngrediente: ddtAcquistoingredientiByIva){
                totaleImponibile = totaleImponibile.add(DdtAcquistoIngrediente.getImponibile());

                BigDecimal partialIva = DdtAcquistoIngrediente.getImponibile().multiply(iva.divide(new BigDecimal(100), RoundingMode.HALF_UP));
                totaleIva = totaleIva.add(partialIva);

                totaleByIva = totaleByIva.add(DdtAcquistoIngrediente.getImponibile());
            }
            totale = totale.add(totaleByIva.add(totaleByIva.multiply(iva.divide(new BigDecimal(100), RoundingMode.HALF_UP))));
        }

        ddtAcquisto.setTotaleImponibile(Utils.roundPrice(totaleImponibile));
        ddtAcquisto.setTotaleIva(Utils.roundPrice(totaleIva));
        ddtAcquisto.setTotale(Utils.roundPrice(totale));
        ddtAcquisto.setTotaleAcconto(new BigDecimal(0));
    }
}

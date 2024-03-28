package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Articolo;
import com.contarbn.model.GiacenzaArticolo;
import com.contarbn.model.Movimentazione;
import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VGiacenzaArticolo;
import com.contarbn.repository.GiacenzaArticoloRepository;
import com.contarbn.repository.views.VGiacenzaArticoloRepository;
import com.contarbn.util.enumeration.Operation;
import com.contarbn.util.enumeration.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class GiacenzaArticoloService {

    private final GiacenzaArticoloRepository giacenzaArticoloRepository;
    private final VGiacenzaArticoloRepository vGiacenzaArticoloRepository;
    private final MovimentazioneService movimentazioneService;
    private final MovimentazioneManualeArticoloService movimentazioneManualeArticoloService;

    public List<VGiacenzaArticolo> getAllByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, String articolo, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto){
        log.info("Retrieving the list of 'giacenze articolo' filtered by request parameters");
        List<VGiacenzaArticolo> giacenze = vGiacenzaArticoloRepository.findByFilters(draw, start, length, sortOrders, articolo, attivo, idFornitore, lotto, scadenza, scaduto);
        log.info("Retrieved {} 'giacenze articolo'", giacenze.size());
        return giacenze;
    }

    public Integer getCountByFilters(String articolo, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto){
        log.info("Retrieving the count of 'giacenze articolo' filtered by request parameters");
        Integer count = vGiacenzaArticoloRepository.countByFilters(articolo, attivo, idFornitore, lotto, scadenza, scaduto);
        log.info("Retrieved {} 'giacenze articolo'", count);
        return count;
    }

    public Set<GiacenzaArticolo> getAllNotAggregate(){
        log.info("Retrieving the list of 'giacenze articolo'");
        Set<GiacenzaArticolo> giacenze = giacenzaArticoloRepository.findAll();
        log.info("Retrieved {} 'giacenze articolo'", giacenze.size());
        return giacenze;
    }

    public List<GiacenzaArticolo> getNotAggregateByArticolo(Long idArticolo){
        log.info("Retrieving the list of 'giacenze articolo' of articolo with id {}", idArticolo);
        Set<GiacenzaArticolo> giacenze = giacenzaArticoloRepository.findByArticoloId(idArticolo);
        giacenze.forEach(this::setScaduto);
        log.info("Retrieved {} 'giacenze articolo'", giacenze.size());
        return giacenze.stream()
                .sorted(Comparator.comparing(GiacenzaArticolo::getLotto).thenComparing(GiacenzaArticolo::getScadenza, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Transactional
    public GiacenzaArticolo create(GiacenzaArticolo giacenzaArticolo){
        log.info("Creating 'giacenza articolo'");

        movimentazioneManualeArticoloService.create(giacenzaArticolo);

        computeGiacenza(giacenzaArticolo.getArticolo().getId(), giacenzaArticolo.getLotto(), giacenzaArticolo.getScadenza());

        log.info("Created 'giacenza articolo' '{}'", giacenzaArticolo);
        return giacenzaArticolo;
    }

    @Transactional
    public void createMovimentazioneManualeArticolo(Long idArticolo, String lotto, Date scadenza, Integer pezzi, Float quantita, Operation operation, Resource resource, Long idDocumento, String numDocumento, Integer annoDocumento, String fornitoreDocumento){

        movimentazioneManualeArticoloService.create(idArticolo, lotto, scadenza, pezzi, quantita, operation, resource, idDocumento, numDocumento, annoDocumento, fornitoreDocumento);
    }

    @Transactional
    public List<GiacenzaArticolo> updateBulk(List<GiacenzaArticolo> giacenzeArticolo){
        log.info("Updating bulk 'giacenza articolo'");

        giacenzeArticolo.forEach(ga -> ga.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant())));

        giacenzaArticoloRepository.saveAll(giacenzeArticolo);

        log.info("Updated bulk 'giacenza articolo'");
        return giacenzeArticolo;
    }

    public void delete(Long idGiacenza){
        log.info("Deleting 'giacenza articolo' '{}'", idGiacenza);
        giacenzaArticoloRepository.deleteById(idGiacenza);
        log.info("Deleted 'giacenza articolo' '{}'", idGiacenza);
    }

    @Transactional
    public void bulkDelete(List<Long> idArticoli){
        log.info("Bulk deleting all the 'giacenze articolo' by 'idArticolo' (number of elements to delete: {})", idArticoli.size());
        movimentazioneManualeArticoloService.deleteByArticoloIdIn(idArticoli);
        giacenzaArticoloRepository.deleteByArticoloIdIn(idArticoli);
        log.info("Bulk deleted all the specified 'giacenze articolo");
    }

    public Map<String, Object> getOne(Long idArticolo){
        log.info("Retrieving 'giacenza articolo' of articolo {}", idArticolo);

        HashMap<String, Object> result = new HashMap<>();

        VGiacenzaArticolo giacenzaArticolo = vGiacenzaArticoloRepository.findById(idArticolo).orElseThrow(ResourceNotFoundException::new);
        Set<GiacenzaArticolo> giacenzeArticoli = giacenzaArticoloRepository.findByArticoloId(idArticolo);

        List<Movimentazione> movimentazioni = new ArrayList<>();
        Set<Movimentazione> movimentazioniArticolo = new HashSet<>();
        if(giacenzeArticoli != null && !giacenzeArticoli.isEmpty()){
            giacenzeArticoli.forEach(ga -> movimentazioniArticolo.addAll(movimentazioneService.getMovimentazioniArticolo(ga.getArticolo().getId(), ga.getLotto(), ga.getScadenza())));
        }
        if(!movimentazioniArticolo.isEmpty()){
            movimentazioni = new ArrayList<>(movimentazioniArticolo);
            movimentazioni.sort(Comparator.comparing(Movimentazione::getData).reversed());
            movimentazioni.sort(Comparator.comparing(Movimentazione::getDataInserimento).reversed());
        }

        result.put("articolo", giacenzaArticolo.getArticolo());
        result.put("unitaMisura", giacenzaArticolo.getUnitaMisura());
        result.put("pezzi", giacenzaArticolo.getPezzi());
        result.put("quantita", giacenzaArticolo.getQuantita());
        result.put("movimentazioni", movimentazioni);

        log.info("Retrieved 'giacenza articolo' of 'articolo' {}", idArticolo);
        return result;
    }

    public void computeGiacenza(Long idArticolo, String lotto, Date scadenza){
        log.info("Compute 'giacenza articolo' for idArticolo '{}', lotto '{}',scadenza '{}'", idArticolo, lotto, scadenza);

        log.info("Retrieving 'giacenza articolo' of articolo '{}' and lotto '{}'", idArticolo, lotto);
        Optional<GiacenzaArticolo> giacenzaOptional = Optional.empty();
        GiacenzaArticolo giacenzaArticolo;
        Set<GiacenzaArticolo> giacenze = giacenzaArticoloRepository.findByArticoloIdAndLotto(idArticolo, lotto);
        if(giacenze != null && !giacenze.isEmpty()){
            if(scadenza != null){
                giacenzaOptional = giacenze.stream().filter(g -> g.getScadenza() != null && g.getScadenza().toLocalDate().compareTo(scadenza.toLocalDate())==0).findFirst();
            } else {
                giacenzaOptional = giacenze.stream().findFirst();
            }
        }

        Set<Movimentazione> movimentazioni = movimentazioneService.getMovimentazioniArticolo(idArticolo, lotto, scadenza);

        Map<String, Object> quantitaAndPezzi = computeQuantitaAndPezzi(movimentazioni);
        int pezzi = (Integer)quantitaAndPezzi.get("pezzi");
        float quantita = (Float)quantitaAndPezzi.get("quantita");

        Timestamp dataInserimento;

        if(giacenzaOptional.isPresent()){
            giacenzaArticolo = giacenzaOptional.get();
            log.info("Updating 'giacenza articolo' {}", giacenzaArticolo);

            dataInserimento = giacenzaArticolo.getDataInserimento();

        } else {
            log.info("Creating a new 'giacenza articolo'");

            giacenzaArticolo = new GiacenzaArticolo();

            dataInserimento = Timestamp.from(ZonedDateTime.now().toInstant());
        }
        Articolo articolo = new Articolo();
        articolo.setId(idArticolo);
        giacenzaArticolo.setArticolo(articolo);
        giacenzaArticolo.setLotto(lotto);
        giacenzaArticolo.setScadenza(scadenza);
        giacenzaArticolo.setQuantita(quantita);
        giacenzaArticolo.setPezzi(pezzi);
        giacenzaArticolo.setDataInserimento(dataInserimento);
        giacenzaArticolo.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));
        giacenzaArticoloRepository.save(giacenzaArticolo);
    }

    public void computeGiacenzaBulk(Integer idArticoloFrom, Integer idArticoloTo){
        Long idArticoloDa = idArticoloFrom != null ? idArticoloFrom.longValue() : -1;
        Long idArticoloA = idArticoloTo != null ? idArticoloTo.longValue() : Long.MAX_VALUE;
        List<GiacenzaArticolo> giacenzeArticoli = giacenzaArticoloRepository.findByArticoloIdFromAndArticoloIdTo(idArticoloDa, idArticoloA);

        for(GiacenzaArticolo giacenzaArticolo : giacenzeArticoli){
            computeGiacenza(giacenzaArticolo.getArticolo().getId(), giacenzaArticolo.getLotto(), giacenzaArticolo.getScadenza());
        }
    }

    public void computeGiacenzaBulk(List<Long> idArticoli){
        Set<GiacenzaArticolo> giacenzeArticoli;
        if(idArticoli != null && !idArticoli.isEmpty()){
            giacenzeArticoli = giacenzaArticoloRepository.findByArticoloIdIn(idArticoli);
        } else {
            giacenzeArticoli = giacenzaArticoloRepository.findAll();
        }

        for(GiacenzaArticolo giacenzaArticolo : giacenzeArticoli){
            computeGiacenza(giacenzaArticolo.getArticolo().getId(), giacenzaArticolo.getLotto(), giacenzaArticolo.getScadenza());
        }
    }

    private Map<String, Object> computeQuantitaAndPezzi(Set<Movimentazione> movimentazioni){
        Map<String, Object> result = new HashMap<>();

        Integer pezziInput;
        Integer pezziOutput;
        int newPezzi;

        Float quantitaInput;
        Float quantitaOutput;
        float newQuantita;

        log.info("Computing input and output quantities");

        if(movimentazioni != null && !movimentazioni.isEmpty()){

            quantitaInput = movimentazioni.stream().filter(m -> m.getInputOutput().equals("INPUT") && m.getQuantita() != null).map(Movimentazione::getQuantita).reduce(0f, Float::sum);
            quantitaOutput = movimentazioni.stream().filter(m -> m.getInputOutput().equals("OUTPUT") && m.getQuantita() != null).map(Movimentazione::getQuantita).reduce(0f, Float::sum);

            newQuantita = quantitaInput - quantitaOutput;

            pezziInput = movimentazioni.stream().filter(m -> m.getInputOutput().equals("INPUT") && m.getPezzi() != null).map(Movimentazione::getPezzi).reduce(0, Integer::sum);
            pezziOutput = movimentazioni.stream().filter(m -> m.getInputOutput().equals("OUTPUT") && m.getPezzi() != null).map(Movimentazione::getPezzi).reduce(0, Integer::sum);

            newPezzi = pezziInput - pezziOutput;

        } else {
            newQuantita = 0f;
            newPezzi = 0;
        }

        result.put("pezzi", newPezzi);
        result.put("quantita", newQuantita);
        return result;
    }

    private void setScaduto(GiacenzaArticolo giacenzaArticolo){
        int scaduto = 0;
        Articolo articolo = giacenzaArticolo.getArticolo();
        if(articolo != null){
            if(giacenzaArticolo.getScadenza() != null){
                int scadenzaGiorniAllarme = articolo.getScadenzaGiorniAllarme() != null ? articolo.getScadenzaGiorniAllarme() : 0;
                LocalDate scadenza = giacenzaArticolo.getScadenza().toLocalDate().minusDays(scadenzaGiorniAllarme);
                if(LocalDate.now().equals(scadenza) || LocalDate.now().isAfter(scadenza)){
                    scaduto = 1;
                }
            }
        }
        giacenzaArticolo.setScaduto(scaduto);
    }
}

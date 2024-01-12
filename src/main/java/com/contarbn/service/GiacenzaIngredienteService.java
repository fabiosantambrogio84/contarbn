package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.GiacenzaIngrediente;
import com.contarbn.model.Ingrediente;
import com.contarbn.model.Movimentazione;
import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VGiacenzaArticolo;
import com.contarbn.model.views.VGiacenzaIngrediente;
import com.contarbn.repository.GiacenzaIngredienteRepository;
import com.contarbn.repository.views.VGiacenzaIngredienteRepository;
import com.contarbn.util.enumeration.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class GiacenzaIngredienteService {

    private final GiacenzaIngredienteRepository giacenzaIngredienteRepository;
    private final VGiacenzaIngredienteRepository vGiacenzaIngredienteRepository;
    private final MovimentazioneService movimentazioneService;
    private final MovimentazioneManualeIngredienteService movimentazioneManualeIngredienteService;

    public Set<VGiacenzaIngrediente> getAll(){
        log.info("Retrieving the list of 'giacenze ingrediente'");
        Set<VGiacenzaIngrediente> giacenze = vGiacenzaIngredienteRepository.findAll();
        log.info("Retrieved {} 'giacenze ingrediente'", giacenze.size());
        return giacenze;
    }

    public Set<GiacenzaIngrediente> getAllNotAggregate(){
        log.info("Retrieving the list of 'giacenze ingrediente'");
        Set<GiacenzaIngrediente> giacenze = giacenzaIngredienteRepository.findAll();
        log.info("Retrieved {} 'giacenze ingrediente'", giacenze.size());
        return giacenze;
    }

    public List<VGiacenzaIngrediente> getAllByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, String ingrediente, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto){
        log.info("Retrieving the list of 'giacenze ingrediente' filtered by request parameters");
        List<VGiacenzaIngrediente> giacenze = vGiacenzaIngredienteRepository.findByFilters(draw, start, length, sortOrders, ingrediente, attivo, idFornitore, lotto, scadenza, scaduto);
        log.info("Retrieved {} 'giacenze ingrediente'", giacenze.size());
        return giacenze;
    }

    public Integer getCountByFilters(String articolo, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto){
        log.info("Retrieving the count of 'giacenze ingrediente' filtered by request parameters");
        Integer count = vGiacenzaIngredienteRepository.countByFilters(articolo, attivo, idFornitore, lotto, scadenza, scaduto);
        log.info("Retrieved {} 'giacenze ingrediente'", count);
        return count;
    }

    @Transactional
    public GiacenzaIngrediente create(GiacenzaIngrediente giacenzaIngrediente){
        log.info("Creating 'giacenza ingrediente'");

        // create movimentazione manuale ingrediente
        movimentazioneManualeIngredienteService.create(giacenzaIngrediente);

        computeGiacenza(giacenzaIngrediente.getIngrediente().getId(), giacenzaIngrediente.getLotto(), giacenzaIngrediente.getScadenza(), giacenzaIngrediente.getQuantita(), Resource.MOVIMENTAZIONE_MANUALE_INGREDIENTE);

        //giacenzaIngrediente.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        //GiacenzaIngrediente createdGiacenzaIngrediente = giacenzaIngredienteRepository.save(giacenzaIngrediente);

        log.info("Created 'giacenza ingrediente' '{}'", giacenzaIngrediente);
        return giacenzaIngrediente;
    }

    public void delete(Long idGiacenza){
        log.info("Deleting 'giacenza ingrediente' '{}'", idGiacenza);
        giacenzaIngredienteRepository.deleteById(idGiacenza);
        log.info("Deleted 'giacenza ingrediente' '{}'", idGiacenza);
    }

    @Transactional
    public void bulkDelete(List<Long> idIngredienti){
        log.info("Bulk deleting all the specified 'giacenze ingrediente' by 'idFornitore' (number of elements to delete: {})", idIngredienti.size());
        movimentazioneManualeIngredienteService.deleteByIngredienteIdIn(idIngredienti);
        giacenzaIngredienteRepository.deleteByIngredienteIdIn(idIngredienti);
        log.info("Bulk deleted all the specified 'giacenze ingrediente");
    }

    public Map<String, Object> getOne(Long idIngrediente){
        log.info("Retrieving 'giacenza ingrediente' of ingrediente {}", idIngrediente);

        HashMap<String, Object> result = new HashMap<>();

        VGiacenzaIngrediente giacenzaIngrediente = vGiacenzaIngredienteRepository.findById(idIngrediente).orElseThrow(ResourceNotFoundException::new);
        Set<GiacenzaIngrediente> giacenzeIngredienti = giacenzaIngredienteRepository.findByIngredienteId(idIngrediente);

        List<Movimentazione> movimentazioni = new ArrayList<>();
        Set<Movimentazione> movimentazioniIngrediente = new HashSet<>();
        if(giacenzeIngredienti != null && !giacenzeIngredienti.isEmpty()){
            giacenzeIngredienti.stream().forEach(gi -> movimentazioniIngrediente.addAll(movimentazioneService.getMovimentazioniIngrediente(gi)));
        }
        if(!movimentazioniIngrediente.isEmpty()){
            movimentazioni = new ArrayList<>(movimentazioniIngrediente);
            movimentazioni.sort(Comparator.comparing(Movimentazione::getData).reversed());
        }

        result.put("ingrediente", giacenzaIngrediente.getIngrediente());
        result.put("quantita", giacenzaIngrediente.getQuantita());
        result.put("udm", giacenzaIngrediente.getUdm());
        result.put("movimentazioni", movimentazioni);

        log.info("Retrieved 'giacenza ingrediente' of ingrediente {}", idIngrediente);
        return result;
    }

    public void computeGiacenza(Long idIngrediente, String lotto, Date scadenza, Float quantita, Resource resource){
        log.info("Compute 'giacenza ingrediente' for idIngrediente '{}', lotto '{}',scadenza '{}',quantita '{}'",
                idIngrediente, lotto, scadenza, quantita);

        log.info("Retrieving 'giacenza ingrediente' of ingrediente '{}' and lotto '{}'", idIngrediente, lotto);
        Optional<GiacenzaIngrediente> giacenzaOptional = Optional.empty();
        GiacenzaIngrediente giacenzaIngrediente;
        Set<GiacenzaIngrediente> giacenze = giacenzaIngredienteRepository.findByIngredienteIdAndLotto(idIngrediente, lotto);
        if(giacenze != null && !giacenze.isEmpty()){
            if(scadenza != null){
                giacenzaOptional = giacenze.stream().filter(g -> g.getScadenza() != null && g.getScadenza().toLocalDate().compareTo(scadenza.toLocalDate())==0).findFirst();
            } else {
                giacenzaOptional = giacenze.stream().findFirst();
            }
        }
        if(giacenzaOptional.isPresent()){
            giacenzaIngrediente = giacenzaOptional.get();
            log.info("Retrieved 'giacenza ingrediente' {}", giacenzaIngrediente);

            Set<Movimentazione> movimentazioni = movimentazioneService.getMovimentazioniIngrediente(giacenzaIngrediente);
            Float quantitaInput;
            Float quantitaOutput;
            float newQuantita;

            log.info("Computing input and output quantities");

            if(movimentazioni != null && !movimentazioni.isEmpty()){
                // 'movimentazioni' in input
                quantitaInput = movimentazioni.stream().filter(m -> m.getInputOutput().equals("INPUT") && m.getQuantita() != null).map(Movimentazione::getQuantita).reduce(0f, Float::sum);

                // 'movimentazioni' in output
                quantitaOutput = movimentazioni.stream().filter(m -> m.getInputOutput().equals("OUTPUT") && m.getQuantita() != null).map(Movimentazione::getQuantita).reduce(0f, Float::sum);

                quantita = (quantita != null ? quantita : 0f);

                switch(resource){
                    case DDT_ACQUISTO:
                    case PRODUZIONE_SCORTA:
                        quantitaInput = quantitaInput + quantita;
                        break;
                    case PRODUZIONE_INGREDIENTE:
                        quantitaOutput = quantitaOutput + quantita;
                        break;
                    default:
                        break;
                }
                newQuantita = quantitaInput - quantitaOutput;
            } else {
                newQuantita = (quantita != null ? (quantita*-1) : 0f);
            }
            giacenzaIngrediente.setQuantita(newQuantita);
            Ingrediente ingrediente = new Ingrediente();
            ingrediente.setId(idIngrediente);
            giacenzaIngrediente.setIngrediente(ingrediente);

            giacenzaIngrediente = giacenzaIngredienteRepository.save(giacenzaIngrediente);
            log.info("Updated 'giacenza ingrediente' {}", giacenzaIngrediente);

        } else {
            log.info("Creating a new 'giacenza ingrediente'");
            float newQuantita = 0f;
            if(quantita != null){
                newQuantita = quantita;
            }
            if(resource.equals(Resource.PRODUZIONE_INGREDIENTE)){
                newQuantita = newQuantita * -1;
            }

            giacenzaIngrediente = new GiacenzaIngrediente();
            Ingrediente ingrediente = new Ingrediente();
            ingrediente.setId(idIngrediente);
            giacenzaIngrediente.setIngrediente(ingrediente);
            giacenzaIngrediente.setLotto(lotto);
            giacenzaIngrediente.setScadenza(scadenza);
            giacenzaIngrediente.setQuantita(newQuantita);
            giacenzaIngrediente.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
            giacenzaIngrediente = giacenzaIngredienteRepository.save(giacenzaIngrediente);
            log.info("Created a new 'giacenza ingrediente' {}", giacenzaIngrediente);
        }

    }
}

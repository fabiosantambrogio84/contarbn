package com.contarbn.service;

import com.contarbn.exception.OperationException;
import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.*;
import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VIngrediente;
import com.contarbn.repository.GiacenzaIngredienteRepository;
import com.contarbn.repository.IngredienteRepository;
import com.contarbn.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class IngredienteService {

    private final IngredienteRepository ingredienteRepository;
    private final GiacenzaIngredienteRepository giacenzaIngredienteRepository;
    private final IngredienteAllergeneService ingredienteAllergeneService;
    private final IngredienteAllergeneComposizioneService ingredienteAllergeneComposizioneService;
    private final RicettaIngredienteService ricettaIngredienteService;
    private final AllergeneService allergeneService;
    private final TransactionTemplate transactionTemplate;

    public List<VIngrediente> getAllByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, String codice, String descrizione, Integer idFornitore, Boolean composto, Boolean attivo){
        log.info("Retrieving the list of 'ingredienti' filtered by request parameters");
        List<VIngrediente> ingredienti = ingredienteRepository.findByFilters(draw, start, length, sortOrders, codice, descrizione, idFornitore, composto, attivo);
        log.info("Retrieved {} 'ingredienti'", ingredienti.size());
        return ingredienti;
    }

    public Integer getCountByFilters(String codice, String descrizione, Integer idFornitore, Boolean composto, Boolean attivo){
        log.info("Retrieving the count of 'ingredienti' filtered by request parameters");
        Integer count = ingredienteRepository.countByFilters(codice, descrizione, idFornitore, composto, attivo);
        log.info("Retrieved {} 'produzioni'", count);
        return count;
    }

    public Ingrediente getOne(Long ingredienteId){
        log.info("Retrieving 'ingrediente' '{}'", ingredienteId);
        Ingrediente ingrediente = ingredienteRepository.findById(ingredienteId).orElseThrow(ResourceNotFoundException::new);
        log.info("Retrieved 'ingrediente' '{}'", ingrediente);
        return ingrediente;
    }

    public Optional<Ingrediente> getByCodice(String codice){
        log.info("Retrieving 'ingrediente' with codice '{}'", codice);
        Optional<Ingrediente> ingrediente = ingredienteRepository.findByCodice(codice);
        if(ingrediente.isPresent()){
            log.info("Retrieved 'ingrediente' '{}'", ingrediente.get());
        } else {
            log.info("'ingrediente' with codice '{}' not existing", codice);
        }
        return ingrediente;
    }

    @Transactional
    public Ingrediente create(Ingrediente ingrediente){
        log.info("Creating 'ingrediente'");
        ingrediente.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        ingrediente.setScadenzaGiorni(ingrediente.getScadenzaGiorni() != null ? ingrediente.getScadenzaGiorni() : Constants.DEFAULT_INGREDIENTE_SCADENZA_GIORNI);

        Ingrediente createdIngrediente = ingredienteRepository.save(ingrediente);
        createdIngrediente.getIngredienteAllergeni().forEach(ia -> {
            ia.getId().setIngredienteId(createdIngrediente.getId());
            ingredienteAllergeneService.create(ia);
        });

        if(createdIngrediente.getComposto()){
            Set<IngredienteAllergeneComposizione> ingredienteAllergeneComposizioni = createIngredienteAllergeneComposizioni(createdIngrediente.getComposizione());
            if(!ingredienteAllergeneComposizioni.isEmpty()){
                ingredienteAllergeneComposizioni.forEach(iac -> {
                    iac.getId().setIngredienteId(createdIngrediente.getId());
                    ingredienteAllergeneComposizioneService.create(iac);
                });
            }
        }

        log.info("Created 'ingrediente' '{}'", createdIngrediente);
        return createdIngrediente;
    }

    @Transactional
    public Ingrediente update(Ingrediente ingrediente){
        log.info("Updating 'ingrediente'");
        Set<IngredienteAllergene> ingredienteAllergeni = ingrediente.getIngredienteAllergeni();
        ingrediente.setIngredienteAllergeni(new HashSet<>());
        ingredienteAllergeneService.deleteByIngredienteId(ingrediente.getId());

        Ingrediente ingredienteCurrent = ingredienteRepository.findById(ingrediente.getId()).orElseThrow(ResourceNotFoundException::new);
        ingrediente.setDataInserimento(ingredienteCurrent.getDataInserimento());
        ingrediente.setScadenzaGiorni(ingrediente.getScadenzaGiorni() != null ? ingrediente.getScadenzaGiorni() : Constants.DEFAULT_INGREDIENTE_SCADENZA_GIORNI);
        Ingrediente updatedIngrediente = ingredienteRepository.save(ingrediente);

        ingredienteAllergeni.forEach(ia -> {
            ia.getId().setIngredienteId(updatedIngrediente.getId());
            ingredienteAllergeneService.create(ia);
        });

        if(updatedIngrediente.getComposto()){
            ingredienteAllergeneComposizioneService.deleteByIngredienteId(updatedIngrediente.getId());
            Set<IngredienteAllergeneComposizione> ingredienteAllergeneComposizioni = createIngredienteAllergeneComposizioni(updatedIngrediente.getComposizione());
            if(!ingredienteAllergeneComposizioni.isEmpty()){
                ingredienteAllergeneComposizioni.forEach(iac -> {
                    iac.getId().setIngredienteId(updatedIngrediente.getId());
                    ingredienteAllergeneComposizioneService.create(iac);
                });
            }
        }

        log.info("Updated 'ingrediente' '{}'", updatedIngrediente);
        return updatedIngrediente;
    }

    @Transactional
    public void delete(Long ingredienteId){

        List<RicettaIngrediente> ricettaIngredienti = ricettaIngredienteService.findByIngredienteId(ingredienteId);
        if(!ricettaIngredienti.isEmpty()){
            throw new OperationException("Errore nella cancellazione: l'ingrediente è presente in ricette");
        }

        try{
            log.info("Deleting 'giacenze' for 'ingrediente' '{}'", ingredienteId);
            giacenzaIngredienteRepository.deleteByIngredienteId(ingredienteId);
            log.info("Deleted 'giacenze' for 'ingrediente' '{}'", ingredienteId);

            log.info("Deleting 'ingrediente' '{}'", ingredienteId);
            ingredienteAllergeneService.deleteByIngredienteId(ingredienteId);
            ingredienteRepository.deleteById(ingredienteId);
            log.info("Deleted 'ingrediente' '{}'", ingredienteId);

        } catch(Exception e){
            throw new OperationException("Errore nella cancellazione dell'ingrediente");
        }
    }

    public void deleteBulk(List<Long> idIngredienti){
        log.info("Bulk deleting all the 'ingredienti' by 'idIngrediente' (number of elements to delete: {})", idIngredienti.size());
        boolean isException = false;
        for(Long idIngrediente : idIngredienti){
            try{
                transactionTemplate.execute(status -> {
                    delete(idIngrediente);
                    return null;
                });
            }catch(Exception e){
                isException = true;
            }
        }
        if(isException){
            throw new OperationException("Alcuni ingredienti selezionati non sono eliminabili perchè presenti in ricette");
        }
        log.info("Bulk deleted all the specified 'ingredienti");
    }

    private Set<IngredienteAllergeneComposizione> createIngredienteAllergeneComposizioni(String composizione){

        Set<IngredienteAllergeneComposizione> ingredienteAllergeneComposizioni = new HashSet<>();

        List<String> allergeniNames = new ArrayList<>();

        Pattern pattern = Pattern.compile("<strong>(.*?)</strong>");

        Matcher matcher = pattern.matcher(composizione);

        while (matcher.find()) {
            String matchedGroup = matcher.group(1);
            String[] words = matchedGroup.split("\\s*,\\s*");

            allergeniNames.addAll(Arrays.asList(words));
        }

        if(!allergeniNames.isEmpty()){
            List<String> allergeniNamesNotExisting = new ArrayList<>();
            for(String allergeneName : allergeniNames){
                Optional<Allergene> allergene = allergeneService.getByNome(allergeneName);
                if(allergene.isPresent()){
                    IngredienteAllergeneComposizioneKey ingredienteAllergeneComposizioneKey = new IngredienteAllergeneComposizioneKey();
                    ingredienteAllergeneComposizioneKey.setAllergeneId(allergene.get().getId());

                    IngredienteAllergeneComposizione ingredienteAllergeneComposizione = new IngredienteAllergeneComposizione();
                    ingredienteAllergeneComposizione.setId(ingredienteAllergeneComposizioneKey);

                    ingredienteAllergeneComposizioni.add(ingredienteAllergeneComposizione);
                } else {
                    allergeniNamesNotExisting.add(allergeneName);
                }
            }
            if(!allergeniNamesNotExisting.isEmpty()){
                throw new OperationException("Composizione contiene allergeni non presenti in anagrafica: "+ String.join(",", allergeniNamesNotExisting));
            }
        }

        return ingredienteAllergeneComposizioni;
    }
}
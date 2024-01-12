package com.contarbn.service;

import com.contarbn.exception.OperationException;
import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Ingrediente;
import com.contarbn.model.IngredienteAllergene;
import com.contarbn.model.RicettaIngrediente;
import com.contarbn.repository.GiacenzaIngredienteRepository;
import com.contarbn.repository.IngredienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class IngredienteService {

    private final IngredienteRepository ingredienteRepository;
    private final GiacenzaIngredienteRepository giacenzaIngredienteRepository;
    private final IngredienteAllergeneService ingredienteAllergeneService;
    private final RicettaIngredienteService ricettaIngredienteService;

    public Set<Ingrediente> getAll(){
        log.info("Retrieving the list of 'ingredienti'");
        Set<Ingrediente> ingredienti = ingredienteRepository.findAll();
        log.info("Retrieved {} 'ingredienti'", ingredienti.size());
        return ingredienti;
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
        Ingrediente createdIngrediente = ingredienteRepository.save(ingrediente);
        createdIngrediente.getIngredienteAllergeni().forEach(ia -> {
            ia.getId().setIngredienteId(createdIngrediente.getId());
            ingredienteAllergeneService.create(ia);
        });
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
        Ingrediente updatedIngrediente = ingredienteRepository.save(ingrediente);

        ingredienteAllergeni.forEach(ia -> {
            ia.getId().setIngredienteId(updatedIngrediente.getId());
            ingredienteAllergeneService.create(ia);
        });

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
}
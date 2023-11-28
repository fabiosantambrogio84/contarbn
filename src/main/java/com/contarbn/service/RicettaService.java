package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Ricetta;
import com.contarbn.model.RicettaAllergene;
import com.contarbn.model.RicettaIngrediente;
import com.contarbn.repository.RicettaRepository;
import com.contarbn.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class RicettaService {

    private final RicettaRepository ricettaRepository;
    private final RicettaIngredienteService ricettaIngredienteService;
    private final RicettaAllergeneService ricettaAllergeneService;
    private final ProduzioneService produzioneService;

    public RicettaService(final RicettaRepository ricettaRepository,
                          final RicettaIngredienteService ricettaIngredienteService,
                          final RicettaAllergeneService ricettaAllergeneService,
                          final ProduzioneService produzioneService){
        this.ricettaRepository = ricettaRepository;
        this.ricettaIngredienteService = ricettaIngredienteService;
        this.ricettaAllergeneService = ricettaAllergeneService;
        this.produzioneService = produzioneService;
    }

    public Set<Ricetta> getAll(){
        log.info("Retrieving the list of 'ricette'");
        Set<Ricetta> ricette = ricettaRepository.findAllByOrderByCodice();
        log.info("Retrieved {} 'ricette'", ricette.size());
        return ricette;
    }

    public Ricetta getOne(Long ricettaId){
        log.info("Retrieving 'ricetta' '{}'", ricettaId);
        Ricetta ricetta = ricettaRepository.findById(ricettaId).orElseThrow(ResourceNotFoundException::new);
        log.info("Retrieved 'ricetta' '{}'", ricetta);
        return ricetta;
    }

    @Transactional
    public Ricetta create(Ricetta ricetta){
        log.info("Creating 'ricetta'");
        Ricetta createdRicetta = ricettaRepository.save(ricetta);
        Float pesoTotale = ricetta.getPesoTotale();
        createdRicetta.getRicettaIngredienti().forEach(ri -> {
            if(ri.getPercentuale() != null){
                ri.setPercentuale(Utils.computePercentuale(ri.getQuantita(), pesoTotale));
            }
            ri.getId().setRicettaId(createdRicetta.getId());
            ricettaIngredienteService.create(ri);
        });
        createdRicetta.getRicettaAllergeni().forEach(ra -> {
            ra.getId().setRicettaId(createdRicetta.getId());
            ricettaAllergeneService.create(ra);
        });
        log.info("Created 'ricetta' '{}'", createdRicetta);
        return createdRicetta;
    }

    @Transactional
    public Ricetta update(Ricetta ricetta){
        log.info("Updating 'ricetta'");
        Set<RicettaIngrediente> ricettaIngredienti = ricetta.getRicettaIngredienti();
        ricetta.setRicettaIngredienti(new HashSet<>());
        ricettaIngredienteService.deleteByRicettaId(ricetta.getId());

        Set<RicettaAllergene> ricettaAllergeni = ricetta.getRicettaAllergeni();
        ricetta.setRicettaAllergeni(new HashSet<>());
        ricettaAllergeneService.deleteByRicettaId(ricetta.getId());

        Ricetta updatedRicetta = ricettaRepository.save(ricetta);
        Float pesoTotale = ricetta.getPesoTotale();
        ricettaIngredienti.forEach(ri -> {
            ri.setPercentuale(Utils.computePercentuale(ri.getQuantita(), pesoTotale));
            ri.getId().setRicettaId(updatedRicetta.getId());
            ricettaIngredienteService.create(ri);
        });
        ricettaAllergeni.forEach(ra -> {
            ra.getId().setRicettaId(updatedRicetta.getId());
            ricettaAllergeneService.create(ra);
        });
        log.info("Updated 'ricetta' '{}'", updatedRicetta);
        return updatedRicetta;
    }

    @Transactional
    public void delete(Long ricettaId){
        log.info("Deleting 'ricetta' '{}'", ricettaId);

        produzioneService.deleteByRicettaId(ricettaId);
        ricettaIngredienteService.deleteByRicettaId(ricettaId);
        ricettaAllergeneService.deleteByRicettaId(ricettaId);
        ricettaRepository.deleteById(ricettaId);
        log.info("Deleted 'ricetta' '{}'", ricettaId);
    }
}
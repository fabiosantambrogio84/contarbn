package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.*;
import com.contarbn.repository.RicettaAnalisiRepository;
import com.contarbn.repository.RicettaNutrienteRepository;
import com.contarbn.repository.RicettaRepository;
import com.contarbn.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class RicettaService {

    private final RicettaRepository ricettaRepository;
    private final RicettaAnalisiRepository ricettaAnalisiRepository;
    private final RicettaNutrienteRepository ricettaNutrienteRepository;
    private final RicettaIngredienteService ricettaIngredienteService;
    private final RicettaAllergeneService ricettaAllergeneService;
    private final ProduzioneService produzioneService;

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
            ri.setPercentuale(Utils.computePercentuale(ri.getQuantita(), pesoTotale));
            ri.getId().setRicettaId(createdRicetta.getId());
            ricettaIngredienteService.create(ri);
        });
        createdRicetta.getRicettaAllergeni().forEach(ra -> {
            ra.getId().setRicettaId(createdRicetta.getId());
            ricettaAllergeneService.create(ra);
        });
        createdRicetta.getRicettaAnalisi().forEach(ra -> {
            ra.getId().setRicettaId(createdRicetta.getId());
            ricettaAnalisiRepository.save(ra);
        });
        createdRicetta.getRicettaNutrienti().forEach(rn -> {
            rn.getId().setRicettaId(createdRicetta.getId());
            ricettaNutrienteRepository.save(rn);
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

        Set<RicettaAnalisi> ricettaAnalisi = ricetta.getRicettaAnalisi();
        ricetta.setRicettaAnalisi(new HashSet<>());
        ricettaAnalisiRepository.deleteByRicettaId(ricetta.getId());

        Set<RicettaNutriente> ricettaNutrienti = ricetta.getRicettaNutrienti();
        ricetta.setRicettaNutrienti(new HashSet<>());
        ricettaNutrienteRepository.deleteByRicettaId(ricetta.getId());

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
        ricettaAnalisi.forEach(ra -> {
            ra.getId().setRicettaId(updatedRicetta.getId());
            ricettaAnalisiRepository.save(ra);
        });
        ricettaNutrienti.forEach(rn -> {
            rn.getId().setRicettaId(updatedRicetta.getId());
            ricettaNutrienteRepository.save(rn);
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
        ricettaAnalisiRepository.deleteByRicettaId(ricettaId);
        ricettaNutrienteRepository.deleteByRicettaId(ricettaId);
        ricettaRepository.deleteById(ricettaId);
        log.info("Deleted 'ricetta' '{}'", ricettaId);
    }

}
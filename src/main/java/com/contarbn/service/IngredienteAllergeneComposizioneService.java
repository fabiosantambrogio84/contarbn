package com.contarbn.service;

import com.contarbn.model.IngredienteAllergeneComposizione;
import com.contarbn.repository.IngredienteAllergeneComposizioneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class IngredienteAllergeneComposizioneService {

    private final IngredienteAllergeneComposizioneRepository ingredienteAllergeneComposizioneRepository;

    public IngredienteAllergeneComposizione create(IngredienteAllergeneComposizione ingredienteAllergeneComposizione){
        log.info("Creating 'ingrediente allergene composizione'");
        IngredienteAllergeneComposizione createdIngredienteAllergeneComposizione = ingredienteAllergeneComposizioneRepository.save(ingredienteAllergeneComposizione);
        log.info("Created 'ingrediente allergene composizione' '{}'", createdIngredienteAllergeneComposizione);
        return createdIngredienteAllergeneComposizione;
    }

    public void deleteByIngredienteId(Long ingredienteId){
        log.info("Deleting 'ingrediente allergene composizione' by 'ingrediente' '{}'", ingredienteId);
        ingredienteAllergeneComposizioneRepository.deleteByIngredienteId(ingredienteId);
        log.info("Deleted 'ingrediente allergene composizione' by 'ingrediente' '{}'", ingredienteId);
    }

}
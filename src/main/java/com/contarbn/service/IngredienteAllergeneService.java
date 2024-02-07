package com.contarbn.service;

import com.contarbn.model.IngredienteAllergene;
import com.contarbn.repository.IngredienteAllergeneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class IngredienteAllergeneService {

    private final IngredienteAllergeneRepository ingredienteAllergeneRepository;

    public IngredienteAllergene create(IngredienteAllergene ingredienteAllergene){
        log.info("Creating 'ingrediente allergene'");
        IngredienteAllergene createdIngredienteAllergene = ingredienteAllergeneRepository.save(ingredienteAllergene);
        log.info("Created 'ingrediente allergene' '{}'", createdIngredienteAllergene);
        return createdIngredienteAllergene;
    }

    public void deleteByIngredienteId(Long ingredienteId){
        log.info("Deleting 'ingrediente allergene' by 'ingrediente' '{}'", ingredienteId);
        ingredienteAllergeneRepository.deleteByIngredienteId(ingredienteId);
        log.info("Deleted 'ingrediente allergene' by 'ingrediente' '{}'", ingredienteId);
    }

}
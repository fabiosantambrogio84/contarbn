package com.contarbn.service;

import com.contarbn.model.RicettaAllergene;
import com.contarbn.model.RicettaIngrediente;
import com.contarbn.repository.RicettaAllergeneRepository;
import com.contarbn.repository.RicettaIngredienteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RicettaAllergeneService {

    private final RicettaAllergeneRepository ricettaAllergeneRepository;

    public RicettaAllergeneService(final RicettaAllergeneRepository ricettaAllergeneRepository){
        this.ricettaAllergeneRepository = ricettaAllergeneRepository;
    }

    public RicettaAllergene create(RicettaAllergene ricettaAllergene){
        log.info("Creating 'ricetta allergene'");
        RicettaAllergene createdRicettaAllergene = ricettaAllergeneRepository.save(ricettaAllergene);
        log.info("Created 'ricetta allergene' '{}'", createdRicettaAllergene);
        return createdRicettaAllergene;
    }

    public void deleteByRicettaId(Long ricettaId){
        log.info("Deleting 'ricetta allergene' by 'ricetta' '{}'", ricettaId);
        ricettaAllergeneRepository.deleteByRicettaId(ricettaId);
        log.info("Deleted 'ricetta allergene' by 'ricetta' '{}'", ricettaId);
    }

}
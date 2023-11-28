package com.contarbn.service;

import com.contarbn.model.RicettaIngrediente;
import com.contarbn.repository.RicettaIngredienteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RicettaIngredienteService {

    private final RicettaIngredienteRepository ricettaIngredienteRepository;

    public RicettaIngredienteService(final RicettaIngredienteRepository ricettaIngredienteRepository){
        this.ricettaIngredienteRepository = ricettaIngredienteRepository;
    }

    public RicettaIngrediente create(RicettaIngrediente ricettaIngrediente){
        log.info("Creating 'ricetta ingrediente'");
        RicettaIngrediente createdRicettaIngrediente = ricettaIngredienteRepository.save(ricettaIngrediente);
        log.info("Created 'ricetta ingrediente' '{}'", createdRicettaIngrediente);
        return createdRicettaIngrediente;
    }

    public void deleteByRicettaId(Long ricettaId){
        log.info("Deleting 'ricetta ingrediente' by 'ricetta' '{}'", ricettaId);
        ricettaIngredienteRepository.deleteByRicettaId(ricettaId);
        log.info("Deleted 'ricetta ingrediente' by 'ricetta' '{}'", ricettaId);
    }

}

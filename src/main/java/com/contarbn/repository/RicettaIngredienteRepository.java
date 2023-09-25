package com.contarbn.repository;

import com.contarbn.model.RicettaIngrediente;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface RicettaIngredienteRepository extends CrudRepository<RicettaIngrediente, Long> {

    @Override
    Set<RicettaIngrediente> findAll();

    Set<RicettaIngrediente> findByRicettaId(Long ricettaId);

    Set<RicettaIngrediente> findByIngredienteId(Long ingredienteId);

    void deleteByRicettaId(Long ricettaId);

    void deleteByIngredienteId(Long ingredienteId);
}

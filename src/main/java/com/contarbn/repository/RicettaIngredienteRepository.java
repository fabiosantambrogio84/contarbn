package com.contarbn.repository;

import com.contarbn.model.RicettaIngrediente;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RicettaIngredienteRepository extends CrudRepository<RicettaIngrediente, Long> {

    List<RicettaIngrediente> findAllByIngredienteId(Long ingredienteId);

    void deleteByRicettaId(Long ricettaId);

}

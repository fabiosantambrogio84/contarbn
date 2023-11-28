package com.contarbn.repository;

import com.contarbn.model.RicettaIngrediente;
import org.springframework.data.repository.CrudRepository;

public interface RicettaIngredienteRepository extends CrudRepository<RicettaIngrediente, Long> {

    void deleteByRicettaId(Long ricettaId);
}

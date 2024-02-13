package com.contarbn.repository;

import com.contarbn.model.RicettaNutriente;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface RicettaNutrienteRepository extends CrudRepository<RicettaNutriente, Long> {

    Set<RicettaNutriente> findByRicettaId(Long idRicetta);

    void deleteByRicettaId(Long idRicetta);
}

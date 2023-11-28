package com.contarbn.repository;

import com.contarbn.model.IngredienteAllergene;
import org.springframework.data.repository.CrudRepository;

public interface IngredienteAllergeneRepository extends CrudRepository<IngredienteAllergene, Long> {

    void deleteByIngredienteId(Long ingredienteId);
}
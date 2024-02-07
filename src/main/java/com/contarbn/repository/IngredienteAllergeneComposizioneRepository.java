package com.contarbn.repository;

import com.contarbn.model.IngredienteAllergeneComposizione;
import org.springframework.data.repository.CrudRepository;

public interface IngredienteAllergeneComposizioneRepository extends CrudRepository<IngredienteAllergeneComposizione, Long> {

    void deleteByIngredienteId(Long ingredienteId);
}
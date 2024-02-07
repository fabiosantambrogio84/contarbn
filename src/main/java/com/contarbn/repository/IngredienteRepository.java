package com.contarbn.repository;

import com.contarbn.model.Ingrediente;
import com.contarbn.repository.custom.IngredienteCustomRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface IngredienteRepository extends CrudRepository<Ingrediente, Long>, IngredienteCustomRepository {

    Optional<Ingrediente> findByCodice(String codice);

}
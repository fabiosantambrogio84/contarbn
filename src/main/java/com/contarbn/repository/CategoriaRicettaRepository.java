package com.contarbn.repository;

import com.contarbn.model.CategoriaRicetta;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface CategoriaRicettaRepository extends CrudRepository<CategoriaRicetta, Long> {

    @Override
    Set<CategoriaRicetta> findAll();

    Set<CategoriaRicetta> findAllByOrderByNome();
}

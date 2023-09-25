package com.contarbn.repository;

import com.contarbn.model.CategoriaArticolo;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface CategoriaArticoloRepository extends CrudRepository<CategoriaArticolo, Long> {

    @Override
    Set<CategoriaArticolo> findAll();

    Set<CategoriaArticolo> findAllByOrderByNome();
}

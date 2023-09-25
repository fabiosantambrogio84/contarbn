package com.contarbn.repository;

import com.contarbn.model.Listino;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface ListinoRepository extends CrudRepository<Listino, Long> {

    @Override
    Set<Listino> findAll();

    Set<Listino> findAllByOrderByTipologia();

    Optional<Listino> findFirstByTipologia(String tipologia);

}

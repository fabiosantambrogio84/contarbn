package com.contarbn.repository;

import com.contarbn.model.UnitaMisura;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface UnitaMisuraRepository extends CrudRepository<UnitaMisura, Long> {

    Set<UnitaMisura> findAllByOrderByEtichetta();

    Optional<UnitaMisura> findByNome(String nome);
}

package com.contarbn.repository;

import com.contarbn.model.StatoFattura;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface StatoFatturaRepository extends CrudRepository<StatoFattura, Long> {

    Set<StatoFattura> findAllByOrderByOrdine();

    Optional<StatoFattura> findByCodice(String codice);

}

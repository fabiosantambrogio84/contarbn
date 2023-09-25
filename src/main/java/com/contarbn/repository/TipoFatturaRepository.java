package com.contarbn.repository;

import com.contarbn.model.TipoFattura;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface TipoFatturaRepository extends CrudRepository<TipoFattura, Long> {

    Set<TipoFattura> findAllByOrderByOrdine();

    Optional<TipoFattura> findByCodice(String codice);

}

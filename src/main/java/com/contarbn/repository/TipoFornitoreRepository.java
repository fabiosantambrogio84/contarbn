package com.contarbn.repository;

import com.contarbn.model.TipoFornitore;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface TipoFornitoreRepository extends CrudRepository<TipoFornitore, Long> {

    Set<TipoFornitore> findAllByOrderByOrdine();

    Optional<TipoFornitore> findByCodice(String codice);

}

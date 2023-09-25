package com.contarbn.repository;

import com.contarbn.model.StatoRicevutaPrivato;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface StatoRicevutaPrivatoRepository extends CrudRepository<StatoRicevutaPrivato, Long> {

    Set<StatoRicevutaPrivato> findAllByOrderByOrdine();

    Optional<StatoRicevutaPrivato> findByCodice(String codice);

}

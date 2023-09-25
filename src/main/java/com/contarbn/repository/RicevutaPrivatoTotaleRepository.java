package com.contarbn.repository;

import com.contarbn.model.RicevutaPrivatoTotale;
import com.contarbn.model.RicevutaPrivatoTotaleKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface RicevutaPrivatoTotaleRepository extends CrudRepository<RicevutaPrivatoTotale, Long> {

    @Override
    Set<RicevutaPrivatoTotale> findAll();

    Optional<RicevutaPrivatoTotale> findById(RicevutaPrivatoTotaleKey id);

    Set<RicevutaPrivatoTotale> findByRicevutaPrivatoId(Long ricevutaPrivatoId);

    void deleteByRicevutaPrivatoId(Long ricevutaPrivatoId);

}

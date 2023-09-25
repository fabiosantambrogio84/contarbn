package com.contarbn.repository;

import com.contarbn.model.Fornitore;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface FornitoreRepository extends CrudRepository<Fornitore, Long> {

    @Override
    Set<Fornitore> findAll();

    Set<Fornitore> findAllByOrderByRagioneSocialeAsc();

    Fornitore findByRagioneSociale(String ragioneSociale);
}

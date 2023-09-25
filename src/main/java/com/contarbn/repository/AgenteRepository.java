package com.contarbn.repository;

import com.contarbn.model.Agente;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface AgenteRepository extends CrudRepository<Agente, Long> {

    @Override
    Set<Agente> findAll();
}

package com.contarbn.repository;

import com.contarbn.model.Confezione;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface ConfezioneRepository extends CrudRepository<Confezione, Long> {

    @Override
    Set<Confezione> findAll();
}

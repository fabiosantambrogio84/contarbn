package com.contarbn.repository;

import com.contarbn.model.Trasportatore;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface TrasportatoreRepository extends CrudRepository<Trasportatore, Long> {

    @Override
    Set<Trasportatore> findAll();
}

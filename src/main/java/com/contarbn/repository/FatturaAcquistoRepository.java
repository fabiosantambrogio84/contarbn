package com.contarbn.repository;

import com.contarbn.model.FatturaAcquisto;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface FatturaAcquistoRepository extends CrudRepository<FatturaAcquisto, Long> {

    @Override
    Set<FatturaAcquisto> findAll();

}

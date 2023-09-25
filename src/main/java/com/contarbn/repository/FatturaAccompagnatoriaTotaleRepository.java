package com.contarbn.repository;

import com.contarbn.model.FatturaAccompagnatoriaTotale;
import com.contarbn.model.FatturaAccompagnatoriaTotaleKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface FatturaAccompagnatoriaTotaleRepository extends CrudRepository<FatturaAccompagnatoriaTotale, Long> {

    @Override
    Set<FatturaAccompagnatoriaTotale> findAll();

    Optional<FatturaAccompagnatoriaTotale> findById(FatturaAccompagnatoriaTotaleKey id);

    Set<FatturaAccompagnatoriaTotale> findByFatturaAccompagnatoriaId(Long fatturaAccompagnatoriaId);

    void deleteByFatturaAccompagnatoriaId(Long fatturaAccompagnatoriaId);

}

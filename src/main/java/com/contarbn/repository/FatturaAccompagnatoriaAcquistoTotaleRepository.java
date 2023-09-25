package com.contarbn.repository;

import com.contarbn.model.FatturaAccompagnatoriaAcquistoTotale;
import com.contarbn.model.FatturaAccompagnatoriaAcquistoTotaleKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface FatturaAccompagnatoriaAcquistoTotaleRepository extends CrudRepository<FatturaAccompagnatoriaAcquistoTotale, Long> {

    @Override
    Set<FatturaAccompagnatoriaAcquistoTotale> findAll();

    Optional<FatturaAccompagnatoriaAcquistoTotale> findById(FatturaAccompagnatoriaAcquistoTotaleKey id);

    Set<FatturaAccompagnatoriaAcquistoTotale> findByFatturaAccompagnatoriaAcquistoId(Long fatturaAccompagnatoriaAcquistoId);

    void deleteByFatturaAccompagnatoriaAcquistoId(Long fatturaAccompagnatoriaAcquistoId);

}

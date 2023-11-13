package com.contarbn.repository;

import com.contarbn.model.FatturaAccompagnatoriaAcquisto;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface FatturaAccompagnatoriaAcquistoRepository extends CrudRepository<FatturaAccompagnatoriaAcquisto, Long> {

    @Override
    Set<FatturaAccompagnatoriaAcquisto> findAll();

    Set<FatturaAccompagnatoriaAcquisto> findAllByOrderByAnnoDescNumeroDesc();

    Optional<FatturaAccompagnatoriaAcquisto> findByFornitoreIdAndNumeroAndIdNot(Long idFornitore, String numero, Long idFatturaAccompagnatoriaAcquisto);

}

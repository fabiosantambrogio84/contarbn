package com.contarbn.repository;

import com.contarbn.model.FatturaAccompagnatoriaAcquistoArticolo;
import com.contarbn.model.FatturaAccompagnatoriaAcquistoArticoloKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface FatturaAccompagnatoriaAcquistoArticoloRepository extends CrudRepository<FatturaAccompagnatoriaAcquistoArticolo, Long> {

    @Override
    Set<FatturaAccompagnatoriaAcquistoArticolo> findAll();

    Optional<FatturaAccompagnatoriaAcquistoArticolo> findById(FatturaAccompagnatoriaAcquistoArticoloKey id);

    Set<FatturaAccompagnatoriaAcquistoArticolo> findByFatturaAccompagnatoriaAcquistoId(Long idFatturaAccompagnatoriaAcquisto);

    Set<FatturaAccompagnatoriaAcquistoArticolo> findByArticoloId(Long articoloId);

    Set<FatturaAccompagnatoriaAcquistoArticolo> findByArticoloIdAndLotto(Long articoloId, String lotto);

    void deleteByFatturaAccompagnatoriaAcquistoId(Long fatturaAccompagnatoriaAcquistoId);

    void deleteByArticoloId(Long articoloId);
}

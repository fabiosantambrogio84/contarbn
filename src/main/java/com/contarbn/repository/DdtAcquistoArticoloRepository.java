package com.contarbn.repository;

import com.contarbn.model.DdtAcquistoArticolo;
import com.contarbn.model.DdtAcquistoArticoloKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface DdtAcquistoArticoloRepository extends CrudRepository<DdtAcquistoArticolo, Long> {

    @Override
    Set<DdtAcquistoArticolo> findAll();

    Optional<DdtAcquistoArticolo> findById(DdtAcquistoArticoloKey id);

    Set<DdtAcquistoArticolo> findByDdtAcquistoId(Long ddtAcquistoId);

    Set<DdtAcquistoArticolo> findByArticoloId(Long articoloId);

    Set<DdtAcquistoArticolo> findByArticoloIdAndLotto(Long articoloId, String lotto);

    void deleteByDdtAcquistoId(Long ddtAcquistoId);

    void deleteByArticoloId(Long articoloId);
}

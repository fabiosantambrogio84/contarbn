package com.contarbn.repository;

import com.contarbn.model.DdtArticolo;
import com.contarbn.model.DdtArticoloKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface DdtArticoloRepository extends CrudRepository<DdtArticolo, Long> {

    @Override
    Set<DdtArticolo> findAll();

    Optional<DdtArticolo> findById(DdtArticoloKey id);

    Set<DdtArticolo> findByDdtId(Long ddtId);

    Set<DdtArticolo> findByArticoloId(Long articoloId);

    Set<DdtArticolo> findByArticoloIdAndLotto(Long articoloId, String lotto);

    void deleteByDdtId(Long ddtId);

    void deleteByArticoloId(Long articoloId);
}

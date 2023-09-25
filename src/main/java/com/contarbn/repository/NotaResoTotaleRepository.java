package com.contarbn.repository;

import com.contarbn.model.NotaResoTotale;
import com.contarbn.model.NotaResoTotaleKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface NotaResoTotaleRepository extends CrudRepository<NotaResoTotale, Long> {

    @Override
    Set<NotaResoTotale> findAll();

    Optional<NotaResoTotale> findById(NotaResoTotaleKey id);

    void deleteByNotaResoId(Long notaResoId);

}

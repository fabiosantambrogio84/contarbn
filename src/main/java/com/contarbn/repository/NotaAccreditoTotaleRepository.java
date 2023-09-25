package com.contarbn.repository;

import com.contarbn.model.NotaAccreditoTotale;
import com.contarbn.model.NotaAccreditoTotaleKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface NotaAccreditoTotaleRepository extends CrudRepository<NotaAccreditoTotale, Long> {

    @Override
    Set<NotaAccreditoTotale> findAll();

    Optional<NotaAccreditoTotale> findById(NotaAccreditoTotaleKey id);

    void deleteByNotaAccreditoId(Long notaAccreditoId);

}

package com.contarbn.repository;

import com.contarbn.model.NotaAccreditoRiga;
import com.contarbn.model.NotaAccreditoRigaKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface NotaAccreditoRigaRepository extends CrudRepository<NotaAccreditoRiga, Long> {

    @Override
    Set<NotaAccreditoRiga> findAll();

    Optional<NotaAccreditoRiga> findById(NotaAccreditoRigaKey id);

    void deleteByNotaAccreditoId(Long notaAccreditoId);

}

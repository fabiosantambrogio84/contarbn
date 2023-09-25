package com.contarbn.repository;

import com.contarbn.model.AliquotaIva;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface AliquotaIvaRepository extends CrudRepository<AliquotaIva, Long> {

    Set<AliquotaIva> findAllByOrderByValore();
}

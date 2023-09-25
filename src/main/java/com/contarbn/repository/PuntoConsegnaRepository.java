package com.contarbn.repository;

import com.contarbn.model.PuntoConsegna;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface PuntoConsegnaRepository extends CrudRepository<PuntoConsegna, Long> {

    @Override
    Set<PuntoConsegna> findAll();

    List<PuntoConsegna> findByClienteId(Long idCliente);

}

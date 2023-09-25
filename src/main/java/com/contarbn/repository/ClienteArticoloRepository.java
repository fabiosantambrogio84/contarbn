package com.contarbn.repository;

import com.contarbn.model.ClienteArticolo;
import com.contarbn.model.ClienteArticoloKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface ClienteArticoloRepository extends CrudRepository<ClienteArticolo, Long> {

    @Override
    Set<ClienteArticolo> findAll();

    Optional<ClienteArticolo> findById(ClienteArticoloKey id);

    Set<ClienteArticolo> findByClienteId(Long clienteId);

    void deleteByClienteId(Long clienteId);

    void deleteByArticoloId(Long articoloId);
}

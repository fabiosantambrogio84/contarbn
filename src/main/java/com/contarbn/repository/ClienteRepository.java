package com.contarbn.repository;

import com.contarbn.model.Cliente;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface ClienteRepository extends CrudRepository<Cliente, Long> {

    @Override
    Set<Cliente> findAll();

    Set<Cliente> findAllByOrderByRagioneSocialeAsc();

    Set<Cliente> findByBloccaDdt(Boolean bloccaDdt);

}

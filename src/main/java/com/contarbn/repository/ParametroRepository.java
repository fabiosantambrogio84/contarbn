package com.contarbn.repository;

import com.contarbn.model.Parametro;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface ParametroRepository extends CrudRepository<Parametro, Long> {

    @Override
    Set<Parametro> findAll();

    Optional<Parametro> findByNome(String nome);

}

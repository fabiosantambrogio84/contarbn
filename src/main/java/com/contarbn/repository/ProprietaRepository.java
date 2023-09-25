package com.contarbn.repository;

import com.contarbn.model.Proprieta;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface ProprietaRepository extends CrudRepository<Proprieta, Long> {

    Set<Proprieta> findAllByOrderByNome();

    Optional<Proprieta> findByNome(String nome);

}

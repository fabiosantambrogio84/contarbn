package com.contarbn.repository;

import com.contarbn.model.SchedaTecnica;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SchedaTecnicaRepository extends CrudRepository<SchedaTecnica, Long> {

    Optional<SchedaTecnica> findFirstByIdRicetta(Long idRicetta);
}

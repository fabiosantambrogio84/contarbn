package com.contarbn.repository;

import com.contarbn.model.RicettaAnalisi;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface RicettaAnalisiRepository extends CrudRepository<RicettaAnalisi, Long> {

    Set<RicettaAnalisi> findByRicettaId(Long idRicetta);

    void deleteByRicettaId(Long idRicetta);
}

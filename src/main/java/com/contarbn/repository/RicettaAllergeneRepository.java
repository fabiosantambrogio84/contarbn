package com.contarbn.repository;

import com.contarbn.model.RicettaAllergene;
import org.springframework.data.repository.CrudRepository;

public interface RicettaAllergeneRepository extends CrudRepository<RicettaAllergene, Long> {

    void deleteByRicettaId(Long ricettaId);
}
package com.contarbn.repository;

import com.contarbn.model.SchedaTecnicaNutriente;
import org.springframework.data.repository.CrudRepository;

public interface SchedaTecnicaNutrienteRepository extends CrudRepository<SchedaTecnicaNutriente, Long> {

    SchedaTecnicaNutriente findBySchedaTecnicaId(Long idSchedaTecnica);

    void deleteBySchedaTecnicaId(Long idSchedaTecnica);
}

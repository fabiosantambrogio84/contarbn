package com.contarbn.repository;

import com.contarbn.model.SchedaTecnicaNutriente;
import org.springframework.data.repository.CrudRepository;

public interface SchedaTecnicaNutrienteRepository extends CrudRepository<SchedaTecnicaNutriente, Long> {

    void deleteBySchedaTecnicaId(Long idSchedaTecnica);
}

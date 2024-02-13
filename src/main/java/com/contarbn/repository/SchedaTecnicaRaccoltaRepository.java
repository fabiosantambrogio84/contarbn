package com.contarbn.repository;

import com.contarbn.model.SchedaTecnicaRaccolta;
import org.springframework.data.repository.CrudRepository;

public interface SchedaTecnicaRaccoltaRepository extends CrudRepository<SchedaTecnicaRaccolta, Long> {

    void deleteBySchedaTecnicaId(Long idSchedaTecnica);
}

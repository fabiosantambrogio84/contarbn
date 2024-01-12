package com.contarbn.repository;

import com.contarbn.model.SchedaTecnicaRaccolta;
import org.springframework.data.repository.CrudRepository;

public interface SchedaTecnicaRaccoltaRepository extends CrudRepository<SchedaTecnicaRaccolta, Long> {

    SchedaTecnicaRaccolta findBySchedaTecnicaId(Long idSchedaTecnica);

    void deleteBySchedaTecnicaId(Long idSchedaTecnica);
}

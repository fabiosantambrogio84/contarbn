package com.contarbn.repository;

import com.contarbn.model.SchedaTecnicaAnalisi;
import org.springframework.data.repository.CrudRepository;

public interface SchedaTecnicaAnalisiRepository extends CrudRepository<SchedaTecnicaAnalisi, Long> {

    void deleteBySchedaTecnicaId(Long idSchedaTecnica);
}

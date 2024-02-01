package com.contarbn.repository;

import com.contarbn.model.SchedaTecnica;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SchedaTecnicaRepository extends CrudRepository<SchedaTecnica, Long> {

    Optional<SchedaTecnica> findFirstByIdProduzioneAndIdArticolo(Long idProduzione, Long idArticolo);

    @Query(nativeQuery = true,
            value = "select num_revisione from scheda_tecnica where anno = ?1 order by num_revisione desc limit 1 for update"
    )
    Integer getLastNumRevisioneByAnno(Integer anno);
}

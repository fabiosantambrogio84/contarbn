package com.contarbn.repository.views;

import com.contarbn.model.views.VSchedaTecnica;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface VSchedaTecnicaRepository extends CrudRepository<VSchedaTecnica, Long> {

    Optional<VSchedaTecnica> findFirstByIdProduzioneAndIdArticolo(Long idProduzione, Long idArticolo);
}

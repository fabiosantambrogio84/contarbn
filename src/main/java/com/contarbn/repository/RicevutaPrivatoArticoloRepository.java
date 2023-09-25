package com.contarbn.repository;

import com.contarbn.model.RicevutaPrivatoArticolo;
import com.contarbn.model.RicevutaPrivatoArticoloKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface RicevutaPrivatoArticoloRepository extends CrudRepository<RicevutaPrivatoArticolo, Long> {

    @Override
    Set<RicevutaPrivatoArticolo> findAll();

    Optional<RicevutaPrivatoArticolo> findById(RicevutaPrivatoArticoloKey id);

    Set<RicevutaPrivatoArticolo> findByRicevutaPrivatoId(Long idRicevutaPrivato);

    Set<RicevutaPrivatoArticolo> findByArticoloId(Long articoloId);

    Set<RicevutaPrivatoArticolo> findByArticoloIdAndLotto(Long articoloId, String lotto);

    void deleteByRicevutaPrivatoId(Long ricevutaPrivatoId);

    void deleteByArticoloId(Long articoloId);
}

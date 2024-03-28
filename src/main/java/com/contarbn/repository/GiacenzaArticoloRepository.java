package com.contarbn.repository;

import com.contarbn.model.GiacenzaArticolo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface GiacenzaArticoloRepository extends CrudRepository<GiacenzaArticolo, Long> {

    @Override
    Set<GiacenzaArticolo> findAll();

    Set<GiacenzaArticolo> findByArticoloIdAndLotto(Long idArticolo, String lotto);

    Set<GiacenzaArticolo> findByArticoloId(Long idArticolo);

    Set<GiacenzaArticolo> findByArticoloIdIn(List<Long> idArticoli);

    @Query(value = "SELECT * FROM giacenza_articolo WHERE id_articolo >= ?1 and id_articolo <=?2 ORDER by id_articolo, lotto", nativeQuery = true)
    List<GiacenzaArticolo> findByArticoloIdFromAndArticoloIdTo(Long idArticoloFrom, Long idArticoloTo);

    void deleteByArticoloId(Long idArticolo);

    void deleteByArticoloIdIn(List<Long> idArticoli);
}

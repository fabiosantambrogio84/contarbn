package com.contarbn.repository;

import com.contarbn.model.GiacenzaArticolo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface GiacenzaArticoloRepository extends CrudRepository<GiacenzaArticolo, Long> {

    @Override
    Set<GiacenzaArticolo> findAll();

    Set<GiacenzaArticolo> findByArticoloIdAndLotto(Long idArticolo, String lotto);

    Set<GiacenzaArticolo> findByArticoloId(Long idArticolo);

    Set<GiacenzaArticolo> findByArticoloIdIn(List<Long> idArticoli);

    void deleteByArticoloId(Long idArticolo);

    void deleteByArticoloIdIn(List<Long> idArticoli);
}

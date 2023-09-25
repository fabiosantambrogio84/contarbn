package com.contarbn.repository.views;

import com.contarbn.model.views.VGiacenzaArticolo;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface VGiacenzaArticoloRepository extends CrudRepository<VGiacenzaArticolo, Long> {

    @Override
    Set<VGiacenzaArticolo> findAll();

    Set<VGiacenzaArticolo> findByIdArticolo(Long idArticolo);

}

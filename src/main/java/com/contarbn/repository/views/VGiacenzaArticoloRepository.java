package com.contarbn.repository.views;

import com.contarbn.model.views.VGiacenzaArticolo;
import com.contarbn.repository.custom.VGiacenzaArticoloCustomRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface VGiacenzaArticoloRepository extends CrudRepository<VGiacenzaArticolo, Long>, VGiacenzaArticoloCustomRepository {

    @Override
    Set<VGiacenzaArticolo> findAll();

}

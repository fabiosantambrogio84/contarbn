package com.contarbn.repository.views;

import com.contarbn.model.views.VFattura;
import com.contarbn.repository.custom.VFatturaCustomRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface VFatturaRepository extends CrudRepository<VFattura, Long>, VFatturaCustomRepository {

    @Override
    Set<VFattura> findAll();

    Set<VFattura> findAllByOrderByAnnoDescProgressivoDesc();

}

package com.contarbn.repository.views;

import com.contarbn.model.views.VGiacenzaIngrediente;
import com.contarbn.repository.custom.VGiacenzaIngredienteCustomRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface VGiacenzaIngredienteRepository extends CrudRepository<VGiacenzaIngrediente, Long>, VGiacenzaIngredienteCustomRepository {

    @Override
    Set<VGiacenzaIngrediente> findAll();

}

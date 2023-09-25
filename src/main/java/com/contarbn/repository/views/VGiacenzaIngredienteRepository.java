package com.contarbn.repository.views;

import com.contarbn.model.views.VGiacenzaIngrediente;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface VGiacenzaIngredienteRepository extends CrudRepository<VGiacenzaIngrediente, Long> {

    @Override
    Set<VGiacenzaIngrediente> findAll();

    Set<VGiacenzaIngrediente> findByIdIngrediente(Long idIngrediente);

}

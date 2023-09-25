package com.contarbn.repository.views;

import com.contarbn.model.views.VDocumentoVenditaIngrediente;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface VDocumentoVenditaIngredienteRepository extends CrudRepository<VDocumentoVenditaIngrediente, Long> {

    @Override
    Set<VDocumentoVenditaIngrediente> findAll();

    Set<VDocumentoVenditaIngrediente> findAllByLottoIngrediente(String lotto);

}

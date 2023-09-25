package com.contarbn.repository.views;

import com.contarbn.model.views.VDocumentoAcquistoIngrediente;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface VDocumentoAcquistoIngredienteRepository extends CrudRepository<VDocumentoAcquistoIngrediente, Long> {

    @Override
    Set<VDocumentoAcquistoIngrediente> findAll();

    Set<VDocumentoAcquistoIngrediente> findAllByLottoIngrediente(String lotto);

}

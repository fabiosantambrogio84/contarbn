package com.contarbn.repository.views;

import com.contarbn.model.views.VProduzioneIngrediente;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface VProduzioneIngredienteRepository extends CrudRepository<VProduzioneIngrediente, Long> {

    @Override
    Set<VProduzioneIngrediente> findAll();

    Set<VProduzioneIngrediente> findAllByLottoIngrediente(String lotto);

}

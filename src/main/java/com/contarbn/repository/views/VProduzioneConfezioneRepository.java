package com.contarbn.repository.views;

import com.contarbn.model.views.VProduzioneConfezione;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface VProduzioneConfezioneRepository extends CrudRepository<VProduzioneConfezione, Long> {

    @Override
    Set<VProduzioneConfezione> findAll();

    Set<VProduzioneConfezione> findAllByLottoConfezione(String lotto);

}

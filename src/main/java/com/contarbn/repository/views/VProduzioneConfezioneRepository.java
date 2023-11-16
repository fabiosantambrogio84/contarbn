package com.contarbn.repository.views;

import com.contarbn.model.views.VProduzioneConfezione;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface VProduzioneConfezioneRepository extends CrudRepository<VProduzioneConfezione, Long> {

    @Override
    Set<VProduzioneConfezione> findAll();

    @Query(nativeQuery = true,
            value = "select * from v_produzione_confezione vpf where vpf.lotto_confezione = ?1 or vpf.lotto_confezione_2 = ?1 or vpf.lotto_film_chiusura = ?1"
    )
    Set<VProduzioneConfezione> findAllByLotto(String lotto);

}

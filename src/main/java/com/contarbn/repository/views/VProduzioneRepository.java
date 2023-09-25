package com.contarbn.repository.views;

import com.contarbn.model.views.VProduzione;
import com.contarbn.repository.custom.VProduzioneCustomRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface VProduzioneRepository extends CrudRepository<VProduzione, Long>, VProduzioneCustomRepository {

    @Override
    Set<VProduzione> findAll();

    Set<VProduzione> findAllByLotto(String lotto);

}

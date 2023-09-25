package com.contarbn.repository;

import com.contarbn.model.ProduzioneConfezione;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface ProduzioneConfezioneRepository extends CrudRepository<ProduzioneConfezione, Long> {

    @Override
    Set<ProduzioneConfezione> findAll();

    Set<ProduzioneConfezione> findByProduzioneId(Long produzioneId);

    Set<ProduzioneConfezione> findByConfezioneId(Long confezioneId);

    Set<ProduzioneConfezione> findByArticoloIdAndLottoProduzione(Long articoloId, String lottoProduzione);

    Set<ProduzioneConfezione> findByIngredienteIdAndLottoProduzione(Long ingredienteId, String lottoProduzione);

    void deleteByProduzioneId(Long produzioneId);

    void deleteByConfezioneId(Long confezioneId);
}

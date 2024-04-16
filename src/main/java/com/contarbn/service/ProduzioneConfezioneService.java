package com.contarbn.service;

import com.contarbn.model.ProduzioneConfezione;
import com.contarbn.repository.ProduzioneConfezioneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProduzioneConfezioneService {

    private final ProduzioneConfezioneRepository produzioneConfezioneRepository;

    public Set<ProduzioneConfezione> findAll(){
        log.info("Retrieving the list of 'produzione confezioni'");
        Set<ProduzioneConfezione> produzioneConfezioni = produzioneConfezioneRepository.findAll();
        log.info("Retrieved {} 'produzione confezioni'", produzioneConfezioni.size());
        return produzioneConfezioni;
    }

    public Set<ProduzioneConfezione> findByProduzioneId(Long produzioneId){
        log.info("Retrieving the list of 'produzione confezioni' for 'produzione' '{}'", produzioneId);
        Set<ProduzioneConfezione> produzioneConfezioni = produzioneConfezioneRepository.findByProduzioneId(produzioneId);
        log.info("Retrieved {} 'produzione confezioni' for 'produzione' '{}'", produzioneConfezioni.size(), produzioneId);
        return produzioneConfezioni;
    }

    public ProduzioneConfezione create(ProduzioneConfezione produzioneConfezione){
        log.info("Creating 'produzione confezione'");
        ProduzioneConfezione createdProduzioneConfezione = produzioneConfezioneRepository.save(produzioneConfezione);
        log.info("Created 'produzione confezione' '{}'", createdProduzioneConfezione);
        return createdProduzioneConfezione;
    }

    public ProduzioneConfezione update(ProduzioneConfezione produzioneConfezione){
        log.info("Updating 'produzione confezione'");
        ProduzioneConfezione updatedProduzioneConfezione = produzioneConfezioneRepository.save(produzioneConfezione);
        log.info("Updated 'produzione confezione' '{}'", updatedProduzioneConfezione);
        return updatedProduzioneConfezione;
    }

    public void deleteByProduzioneId(Long produzioneId){
        log.info("Deleting 'produzione confezione' by 'produzione' '{}'", produzioneId);
        produzioneConfezioneRepository.deleteByProduzioneId(produzioneId);
        log.info("Deleted 'produzione confezione' by 'produzione' '{}'", produzioneId);
    }

}
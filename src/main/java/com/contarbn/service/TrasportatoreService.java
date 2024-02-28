package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Trasportatore;
import com.contarbn.repository.TrasportatoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrasportatoreService {

    private final TrasportatoreRepository trasportatoreRepository;

    public Set<Trasportatore> getAll(){
        log.info("Retrieving the list of 'trasportatori'");
        Set<Trasportatore> trasportatori = trasportatoreRepository.findAll();
        log.info("Retrieved {} 'trasportatori'", trasportatori.size());
        return trasportatori;
    }

    public Trasportatore getOne(Long trasportatoreId){
        log.info("Retrieving 'trasportatore' '{}'", trasportatoreId);
        Trasportatore trasportatore = trasportatoreRepository.findById(trasportatoreId).orElseThrow(ResourceNotFoundException::new);
        log.info("Retrieved 'trasportatore' '{}'", trasportatore);
        return trasportatore;
    }

    public Trasportatore create(Trasportatore trasportatore){
        log.info("Creating 'trasportatore'");
        Trasportatore createdTrasportatore = trasportatoreRepository.save(trasportatore);
        log.info("Created 'trasportatore' '{}'", createdTrasportatore);
        return createdTrasportatore;
    }

    public Trasportatore update(Trasportatore trasportatore){
        log.info("Updating 'trasportatore'");
        Trasportatore updatedTrasportatore = trasportatoreRepository.save(trasportatore);
        log.info("Updated 'trasportatore' '{}'", updatedTrasportatore);
        return updatedTrasportatore;
    }

    public void delete(Long trasportatoreId){
        log.info("Deleting 'trasportatore' '{}'", trasportatoreId);
        trasportatoreRepository.deleteById(trasportatoreId);
        log.info("Deleted 'trasportatore' '{}'", trasportatoreId);
    }
}
package com.contarbn.service;

import com.contarbn.exception.OperationException;
import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Allergene;
import com.contarbn.repository.AllergeneRepository;
import com.contarbn.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@Service
public class AllergeneService {

    private final AllergeneRepository allergeneRepository;

    public AllergeneService(final AllergeneRepository allergeneRepository){
        this.allergeneRepository = allergeneRepository;
    }

    public List<Allergene> getAll(Boolean attivo){
        log.info("Retrieving the list of 'allergene'");
        List<Allergene> allergeni = allergeneRepository.findAllByAttivo(Utils.getActiveValues(attivo));
        log.info("Retrieved {} 'allergene'", allergeni.size());
        return allergeni;
    }

    public Allergene getOne(Long allergeneId){
        log.info("Retrieving 'allergene' '{}'", allergeneId);
        Allergene allergene = allergeneRepository.findById(allergeneId).orElseThrow(ResourceNotFoundException::new);
        log.info("Retrieved 'allergene' '{}'", allergene);
        return allergene;
    }

    public Allergene create(Allergene allergene){
        log.info("Creating 'allergene'");
        allergene.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        allergene.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));

        checkExistingOrdine(allergene.getOrdine(), -1L);

        Allergene createdAllergene = allergeneRepository.save(allergene);
        log.info("Created 'allergene' '{}'", createdAllergene);
        return createdAllergene;
    }

    public Allergene update(Allergene allergene){
        log.info("Updating 'allergene'");

        Allergene currentAllergene = allergeneRepository.findById(allergene.getId()).orElseThrow(ResourceNotFoundException::new);
        allergene.setDataInserimento(currentAllergene.getDataInserimento());
        allergene.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));

        checkExistingOrdine(allergene.getOrdine(), allergene.getId());

        Allergene updatedAllergene = allergeneRepository.save(allergene);

        log.info("Updated 'allergene' '{}'", updatedAllergene);
        return updatedAllergene;
    }

    public void delete(Long allergeneId){
        log.info("Deleting 'allergene' '{}'", allergeneId);
        allergeneRepository.deleteById(allergeneId);
        log.info("Deleted 'allergene' '{}'", allergeneId);
    }

    private void checkExistingOrdine(Integer ordine, Long id){
        Optional<Allergene> allergeneByOrdineAndIdNot = allergeneRepository.findByOrdineAndIdNot(ordine, id);
        if(allergeneByOrdineAndIdNot.isPresent()){
            throw new OperationException("Allergene con ordine "+ordine+" già presente", BAD_REQUEST);
        }
    }

}
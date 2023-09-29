package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Autista;
import com.contarbn.model.views.VDdtLast;
import com.contarbn.repository.AutistaRepository;
import com.contarbn.repository.views.VDdtLastRepository;
import com.contarbn.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class AutistaService {

    private final AutistaRepository autistaRepository;

    private final VDdtLastRepository vDdtLastRepository;

    @Autowired
    public AutistaService(final AutistaRepository autistaRepository,
                          final VDdtLastRepository vDdtLastRepository){
        this.autistaRepository = autistaRepository;
        this.vDdtLastRepository = vDdtLastRepository;
    }

    public Set<Autista> getAll(){
        log.info("Retrieving the list of 'autisti'");
        Set<Autista> autisti = autistaRepository.findAll();
        if(!autisti.isEmpty()){
            Optional<VDdtLast> vDdtLast = vDdtLastRepository.find();
            for(Autista autista : autisti){
                if(vDdtLast.isPresent()){
                    if(autista.getId().equals(vDdtLast.get().getIdAutista())){
                        autista.setPredefinito(true);
                    }
                } else {
                    if(autista.getCognome().equalsIgnoreCase(Constants.DEFAULT_AUTISTA_COGNOME)){
                        autista.setPredefinito(true);
                    }
                }
            }
        }
        log.info("Retrieved {} 'autisti'", autisti.size());
        return autisti;
    }

    public Autista getOne(Long autistaId){
        log.info("Retrieving 'autista' '{}'", autistaId);
        Autista autista = autistaRepository.findById(autistaId).orElseThrow(ResourceNotFoundException::new);
        log.info("Retrieved 'autista' '{}'", autista);
        return autista;
    }

    public Autista create(Autista autista){
        log.info("Creating 'autista'");
        Autista createdAutista = autistaRepository.save(autista);
        log.info("Created 'autista' '{}'", createdAutista);
        return createdAutista;
    }

    public Autista update(Autista autista){
        log.info("Updating 'autista'");
        Autista updatedAutista = autistaRepository.save(autista);
        log.info("Updated 'autista' '{}'", updatedAutista);
        return updatedAutista;
    }

    public void delete(Long autistaId){
        log.info("Disabling 'autista' '{}'", autistaId);
        Autista autista = autistaRepository.findById(autistaId).orElseThrow(ResourceNotFoundException::new);
        autista.setAttivo(false);
        autistaRepository.save(autista);
        log.info("Disabled 'autista' '{}'", autistaId);
    }
}

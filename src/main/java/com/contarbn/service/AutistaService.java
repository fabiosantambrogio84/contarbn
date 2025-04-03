package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Autista;
import com.contarbn.model.views.VDocumentoLast;
import com.contarbn.repository.AutistaRepository;
import com.contarbn.repository.views.VDocumentoLastRepository;
import com.contarbn.util.Constants;
import com.contarbn.util.enumeration.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Service
public class AutistaService {

    private final AutistaRepository autistaRepository;

    private final VDocumentoLastRepository vDocumentoLastRepository;

    public Set<Autista> getAll(String context){
        log.info("Retrieving the list of 'autisti'");
        Set<Autista> autisti = autistaRepository.findAll();
        if(!autisti.isEmpty()){
            Optional<VDocumentoLast> vDocumentoLast = vDocumentoLastRepository.find(StringUtils.isNotEmpty(context) ? context : Resource.DDT.getParam());
            for(Autista autista : autisti){
                if(vDocumentoLast.isPresent()){
                    if(autista.getId().equals(vDocumentoLast.get().getIdAutista())){
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

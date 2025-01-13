package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.OrdineCliente;
import com.contarbn.model.Telefonata;
import com.contarbn.repository.TelefonataRepository;
import com.contarbn.util.enumeration.GiornoSettimana;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
public class TelefonataService {

    private final TelefonataRepository telefonataRepository;

    private final OrdineClienteService ordineClienteService;

    @Autowired
    public TelefonataService(final TelefonataRepository telefonataRepository,
                             final OrdineClienteService ordineClienteService){
        this.telefonataRepository = telefonataRepository;
        this.ordineClienteService = ordineClienteService;
    }

    public List<Telefonata> getAll(){
        log.info("Retrieving the list of 'telefonate'");
        List<Telefonata> telefonate = telefonataRepository.findAll();
        log.info("Retrieved {} 'telefonate'", telefonate.size());
        return telefonate;
    }

    public Telefonata getOne(Long telefonataId){
        log.info("Retrieving 'telefonata' '{}'", telefonataId);
        Telefonata telefonata = telefonataRepository.findById(telefonataId).orElseThrow(ResourceNotFoundException::new);
        log.info("Retrieved 'telefonata' '{}'", telefonata);
        return telefonata;
    }

    public Telefonata create(Telefonata telefonata){
        log.info("Creating 'telefonata'");
        telefonata.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        if(telefonata.getGiornoOrdinale() == null){
            telefonata.setGiornoOrdinale(GiornoSettimana.getValueByLabel(telefonata.getGiorno()));
        }
        if(telefonata.getGiornoConsegnaOrdinale() == null){
            telefonata.setGiornoConsegnaOrdinale(GiornoSettimana.getValueByLabel(telefonata.getGiornoConsegna()));
        }
        Telefonata createdTelefonata = telefonataRepository.save(telefonata);
        log.info("Created 'telefonata' '{}'", createdTelefonata);

        return telefonata;
    }

    public Telefonata update(Telefonata telefonata){
        log.info("Updating 'telefonata'");
        Telefonata telefonataCurrent = telefonataRepository.findById(telefonata.getId()).orElseThrow(ResourceNotFoundException::new);
        telefonata.setDataInserimento(telefonataCurrent.getDataInserimento());
        Telefonata updatedTelefonata = telefonataRepository.save(telefonata);
        log.info("Updated 'telefonata' '{}'", updatedTelefonata);
        return updatedTelefonata;
    }

    public Telefonata patch(Map<String,Object> patchTelefonata){
        log.info("Patching 'telefonata'");

        Long idTelefonata = Long.valueOf((Integer) patchTelefonata.get("idTelefonata"));
        Boolean eseguito = (Boolean)patchTelefonata.get("eseguito");

        Telefonata telefonata = telefonataRepository.findById(idTelefonata).orElseThrow(ResourceNotFoundException::new);
        telefonata.setEseguito(eseguito);
        if(Boolean.TRUE.equals(eseguito)){
            telefonata.setDataEsecuzione(Timestamp.from(ZonedDateTime.now().toInstant()));
        }
        Telefonata patchedTelefonata = telefonataRepository.save(telefonata);

        log.info("Patched 'telefonata' '{}'", patchedTelefonata);
        return patchedTelefonata;
    }

    public void updateAfterDeletePuntoConsegna(Long puntoConsegnaId){
        log.info("Updating 'telefonate' after delete of 'puntoConsegna' '{}'", puntoConsegnaId);
        telefonataRepository.findByPuntoConsegnaId(puntoConsegnaId).stream().forEach(t -> {
            t.setPuntoConsegna(null);
            update(t);
        });
        log.info("Update 'telefonate' after delete of 'puntoConsegna' '{}'", puntoConsegnaId);
    }

    @Transactional
    public void delete(Long telefonataId){
        log.info("Deleting 'telefonata' '{}'", telefonataId);
        deleteOrdiniClienti(telefonataId);
        telefonataRepository.deleteById(telefonataId);
        log.info("Deleted 'telefonata' '{}'", telefonataId);
    }

    public void deleteByClienteId(Long clienteId){
        log.info("Deleting all 'telefonate' of 'cliente' '{}'", clienteId);
        telefonataRepository.deleteByClienteId(clienteId);
        log.info("Deleted all 'telefonate' of 'cliente' '{}'", clienteId);
    }

    @Transactional
    public void bulkDelete(List<Long> telefonateIds){
        log.info("Bulk deleting all the specified 'telefonate (number of elements to delete: {})'", telefonateIds.size());
        if(!telefonateIds.isEmpty()){
            for(Long idTelefonata : telefonateIds){
                deleteOrdiniClienti(idTelefonata);
            }
        }
        telefonataRepository.deleteByIdIn(telefonateIds);
        log.info("Bulk deleted all the specified 'telefonate");
    }

    public void bulkSetEseguito(List<Long> telefonateIds, Boolean eseguito){
        if(!telefonateIds.isEmpty()) {
            List<Telefonata> telefonate = new ArrayList<>();
            for(Long idTelefonata : telefonateIds){
                Optional<Telefonata> telefonataOptional = telefonataRepository.findById(idTelefonata);
                if(telefonataOptional.isPresent()){
                    Telefonata telefonata = telefonataOptional.get();
                    telefonata.setEseguito(eseguito);
                    telefonate.add(telefonata);
                }
            }
            if(!telefonate.isEmpty()){
                telefonataRepository.saveAll(telefonate);
            }
        }
    }

    private void deleteOrdiniClienti(Long idTelefonata){
        Set<OrdineCliente> ordiniClienti = ordineClienteService.getByIdTelefonata(idTelefonata);
        if(ordiniClienti != null && !ordiniClienti.isEmpty()){
            for(OrdineCliente ordineCliente : ordiniClienti){
                Map<String,Object> map = new HashMap();
                map.put("id", ordineCliente.getId().intValue());
                map.put("idTelefonata", null);

                ordineClienteService.patch(map);
            }
        }
    }
}

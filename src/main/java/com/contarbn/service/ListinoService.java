package com.contarbn.service;

import com.contarbn.exception.ListinoBaseAlreadyExistingException;
import com.contarbn.exception.ListinoTipologiaNotAllowedException;
import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.*;
import com.contarbn.repository.ListinoRepository;
import com.contarbn.util.enumeration.TipologiaListino;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ListinoService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ListinoService.class);

    private final ListinoRepository listinoRepository;

    private final ArticoloService articoloService;

    private final ListinoPrezzoService listinoPrezzoService;

    private final ListinoPrezzoVariazioneService listinoPrezzoVariazioneService;

    private final static String TIPOLOGIA_BASE = "BASE";

    @Autowired
    public ListinoService(final ListinoRepository listinoRepository, final ArticoloService articoloService, final ListinoPrezzoService listinoPrezzoService, final ListinoPrezzoVariazioneService listinoPrezzoVariazioneService){
        this.listinoRepository = listinoRepository;
        this.articoloService = articoloService;
        this.listinoPrezzoService = listinoPrezzoService;
        this.listinoPrezzoVariazioneService = listinoPrezzoVariazioneService;
    }

    public Set<Listino> getAll(){
        LOGGER.info("Retrieving the list of 'listini'");
        Set<Listino> listini = listinoRepository.findAllByOrderByTipologia();
        LOGGER.info("Retrieved {} 'listini'", listini.size());
        return listini;
    }

    public Listino getOne(Long listinoId){
        LOGGER.info("Retrieving 'listino' '{}'", listinoId);
        Listino listino = listinoRepository.findById(listinoId).orElseThrow(ResourceNotFoundException::new);
        LOGGER.info("Retrieved 'listino' '{}'", listino);
        return listino;
    }

    public List<ListinoPrezzo> getListiniPrezziByListinoId(Long listinoId){
        LOGGER.info("Retrieving 'listiniPrezzi' of 'listino' '{}'", listinoId);
        List<ListinoPrezzo> listiniPrezzi = listinoPrezzoService.getByListinoId(listinoId);
        LOGGER.info("Retrieved '{}' 'listiniPrezzi'", listiniPrezzi.size());
        return listiniPrezzi;
    }

    public List<ListinoPrezzoVariazione> getListiniPrezziVariazioniByListinoId(Long listinoId){
        LOGGER.info("Retrieving 'listiniPrezziVariazioni' of 'listino' '{}'", listinoId);
        List<ListinoPrezzoVariazione> listiniPrezziVariazioni = listinoPrezzoVariazioneService.getByListinoId(listinoId);
        LOGGER.info("Retrieved '{}' 'listiniPrezziVariazioni'", listiniPrezziVariazioni.size());
        return listiniPrezziVariazioni;
    }

    @Transactional
    public Listino create(Listino listino){
        LOGGER.info("Creating 'listino'");
        checkListinoTipologia(listino.getTipologia());
        if(listino.getTipologia().equals(TipologiaListino.BASE.name())){
            if(checkIfListinoBaseExists()){
                throw new ListinoBaseAlreadyExistingException();
            }
        }
        listino.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        //listino.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));
        Listino createdListino = listinoRepository.save(listino);
        LOGGER.info("Created 'listino' '{}'", createdListino);

        return createdListino;
    }

    @Transactional
    public Listino update(Listino listino){
        LOGGER.info("Updating 'listino' with id '{}'", listino.getId());
        // check the tipologia of listino
        checkListinoTipologia(listino.getTipologia());

        if(listino.getTipologia().equals(TipologiaListino.BASE.name())){
            if(checkIfListinoBaseExists()){
                throw new ListinoBaseAlreadyExistingException();
            }
            listino.setListiniPrezziVariazioni(null);
        }
        Listino listinoCurrent = listinoRepository.findById(listino.getId()).orElseThrow(ResourceNotFoundException::new);
        listino.setDataInserimento(listinoCurrent.getDataInserimento());
        listino.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));
        Listino updatedListino = listinoRepository.save(listino);
        LOGGER.info("Updated 'listino' '{}'", updatedListino);

        return updatedListino;
    }

    @Transactional
    public void createListiniVariazioniPrezzi(Listino listino, List<ListinoPrezzoVariazione> listiniPrezziVariazioni){
        LOGGER.info("Creating 'listiniPrezziVariazioni' for 'listino' with id '{}'", listino.getId());
        if(!CollectionUtils.isEmpty(listiniPrezziVariazioni)){
            listinoPrezzoVariazioneService.create(listiniPrezziVariazioni);

            LOGGER.info("Populating 'listiniPrezzi' for listino '{}'", listino.getId());
            final String tipologiaVariazionePrezzo = listiniPrezziVariazioni.stream().filter(lpv -> lpv.getTipologiaVariazionePrezzo() != null).map(ListinoPrezzoVariazione::getTipologiaVariazionePrezzo).findFirst().orElse(null);
            final Float variazionePrezzo = listiniPrezziVariazioni.stream().filter(lpv -> lpv.getVariazionePrezzo() != null).map(ListinoPrezzoVariazione::getVariazionePrezzo).findFirst().orElse(null);

            List<ListinoPrezzo> listiniPrezzi = new ArrayList<>();

            Set<Articolo> articoli = articoloService.getAll();
            articoli.forEach(a -> {
                ListinoPrezzo listinoPrezzo = new ListinoPrezzo();
                listinoPrezzo.setArticolo(a);
                listinoPrezzo.setListino(listino);
                listinoPrezzo.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
                BigDecimal newPrezzo = listinoPrezzoService.computePrezzoInListinoCreation(listino, a, tipologiaVariazionePrezzo, variazionePrezzo);

                listinoPrezzo.setPrezzo(newPrezzo);

                listiniPrezzi.add(listinoPrezzo);
            });

            if(!CollectionUtils.isEmpty(listiniPrezzi)){
                listinoPrezzoService.create(listiniPrezzi);
            }
            LOGGER.info("Successfully populated 'listiniPrezzi' for listino '{}'", listino.getId());
        } else {
            LOGGER.info("Specified 'listiniPrezziVariazioni' is null or empty");
        }
    }

    @Transactional
    public void recreateListiniVariazioniPrezzi(Listino listino, List<ListinoPrezzoVariazione> listiniPrezziVariazioni){
        LOGGER.info("Recreating 'listiniPrezziVariazioni' for 'listino' with id '{}'", listino.getId());

        listinoPrezzoVariazioneService.deleteByListinoId(listino.getId());
        if(!CollectionUtils.isEmpty(listiniPrezziVariazioni)){
            listinoPrezzoVariazioneService.create(listiniPrezziVariazioni);
        }
        LOGGER.info("Updating 'listiniPrezzi' for listino '{}'", listino.getId());
        String tipologiaVariazionePrezzo = null;
        Float variazionePrezzo = null;
        List<Long> articoliVariazioni = null;
        Fornitore fornitoreVariazione = null;
        if(!CollectionUtils.isEmpty(listiniPrezziVariazioni)){
            tipologiaVariazionePrezzo = listiniPrezziVariazioni.stream().filter(lpv -> lpv.getTipologiaVariazionePrezzo() != null).map(ListinoPrezzoVariazione::getTipologiaVariazionePrezzo).findFirst().orElse(null);
            variazionePrezzo = listiniPrezziVariazioni.stream().filter(lpv -> lpv.getVariazionePrezzo() != null).map(ListinoPrezzoVariazione::getVariazionePrezzo).findFirst().orElse(null);
            fornitoreVariazione = listiniPrezziVariazioni.stream().filter(lpv -> lpv.getFornitore() != null).map(ListinoPrezzoVariazione::getFornitore).findFirst().orElse(null);
            articoliVariazioni = listiniPrezziVariazioni.stream().filter(lpv -> lpv.getArticolo() != null).map(lpv -> lpv.getArticolo().getId()).collect(Collectors.toList());
        }
        LOGGER.info("Retrieved 'variazionePrezzo':'{}', 'fornitore':'{}', 'articoli' size: '{}'", variazionePrezzo, fornitoreVariazione != null ? fornitoreVariazione.getId() : null, articoliVariazioni != null ? articoliVariazioni.size() : null);

        List<ListinoPrezzo> listiniPrezziToUpdate;
        if(variazionePrezzo != null && !CollectionUtils.isEmpty(articoliVariazioni) && fornitoreVariazione != null){
            listiniPrezziToUpdate = listinoPrezzoService.getByListinoIdAndArticoloIdInAndFornitoreId(listino.getId(), articoliVariazioni, fornitoreVariazione.getId());
        } else if(variazionePrezzo != null && !CollectionUtils.isEmpty(articoliVariazioni)){
            listiniPrezziToUpdate = listinoPrezzoService.getByListinoIdAndArticoloIdIn(listino.getId(), articoliVariazioni);
        } else if(variazionePrezzo != null && fornitoreVariazione != null){
            listiniPrezziToUpdate = listinoPrezzoService.getByListinoIdAndArticoloFornitoreId(listino.getId(), fornitoreVariazione.getId());
        } else {
            listiniPrezziToUpdate = listinoPrezzoService.getByListinoId(listino.getId());
        }

        if(!CollectionUtils.isEmpty(listiniPrezziToUpdate)){
            insertOrUpdateListiniPrezzi(listiniPrezziToUpdate, tipologiaVariazionePrezzo, variazionePrezzo);
        }
        LOGGER.info("Updated 'listiniPrezzi' for listino '{}'", listino.getId());
    }

    @Transactional
    public Listino duplicate(Long listinoId, Map<String, String> body){
        Listino listino = getOne(listinoId);
        List<ListinoPrezzo> listinoPrezzi = getListiniPrezziByListinoId(listinoId);
        List<ListinoPrezzoVariazione> listinoPrezziVariazioni = getListiniPrezziVariazioniByListinoId(listinoId);

        Listino newListino = new Listino();
        newListino.setNome(body.get("name"));
        newListino.setTipologia(listino.getTipologia());
        newListino.setNote(listino.getNote());
        newListino.setBloccaPrezzi(listino.getBloccaPrezzi());
        newListino.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        newListino = listinoRepository.save(newListino);

        List<ListinoPrezzo> newListinoPrezzi = new ArrayList<>();
        for(ListinoPrezzo listinoPrezzo : listinoPrezzi){
            ListinoPrezzo newListinoPrezzo = new ListinoPrezzo();
            newListinoPrezzo.setListino(newListino);
            newListinoPrezzo.setArticolo(listinoPrezzo.getArticolo());
            newListinoPrezzo.setPrezzo(listinoPrezzo.getPrezzo());
            newListinoPrezzo.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));

            newListinoPrezzi.add(newListinoPrezzo);
        }
        listinoPrezzoService.create(newListinoPrezzi);

        List<ListinoPrezzoVariazione> newListinoPrezziVariazioni = new ArrayList<>();
        for(ListinoPrezzoVariazione listinoPrezzoVariazione : listinoPrezziVariazioni){
            ListinoPrezzoVariazione newListinoPrezzoVariazione = new ListinoPrezzoVariazione();
            newListinoPrezzoVariazione.setListino(newListino);
            newListinoPrezzoVariazione.setArticolo(listinoPrezzoVariazione.getArticolo());
            newListinoPrezzoVariazione.setFornitore(listinoPrezzoVariazione.getFornitore());
            newListinoPrezzoVariazione.setVariazionePrezzo(listinoPrezzoVariazione.getVariazionePrezzo());
            newListinoPrezzoVariazione.setTipologiaVariazionePrezzo(listinoPrezzoVariazione.getTipologiaVariazionePrezzo());

            newListinoPrezziVariazioni.add(newListinoPrezzoVariazione);
        }
        listinoPrezzoVariazioneService.create(newListinoPrezziVariazioni);

        return newListino;
    }

    @Transactional
    public void delete(Long listinoId){
        LOGGER.info("Deleting 'listiniPrezziVariazioni' of listino '{}'", listinoId);
        listinoPrezzoVariazioneService.deleteByListinoId(listinoId);
        LOGGER.info("Deleted 'listiniPrezziVariazioni' of listino '{}'", listinoId);

        LOGGER.info("Deleting 'listiniPrezzi' of listino '{}'", listinoId);
        listinoPrezzoService.deleteByListinoId(listinoId);
        LOGGER.info("Deleted 'listiniPrezzi' of listino '{}'", listinoId);

        LOGGER.info("Deleting 'listino' '{}'", listinoId);
        listinoRepository.deleteById(listinoId);
        LOGGER.info("Deleted 'listino' '{}'", listinoId);
    }

    private boolean checkIfListinoBaseExists(){
        Optional<Listino> listino = listinoRepository.findFirstByTipologia(TIPOLOGIA_BASE);
        return listino.isPresent();
    }

    private void checkListinoTipologia(String tipologia){
        try {
            TipologiaListino.valueOf(tipologia);
        } catch (Exception e){
            throw new ListinoTipologiaNotAllowedException();
        }
    }

    private void insertOrUpdateListiniPrezzi(List<ListinoPrezzo> listiniPrezzi, String tipologiaVariazionePrezzo, Float variazionePrezzo){
        listiniPrezzi.forEach(lp -> {
            lp.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));
            lp.setPrezzo(listinoPrezzoService.computePrezzo(lp.getArticolo(), tipologiaVariazionePrezzo, variazionePrezzo));
        });
        listinoPrezzoService.bulkInsertOrUpdate(listiniPrezzi);
    }

}

package com.contarbn.service;

import com.contarbn.exception.ArticoloBarcodeCannotStartWithZeroException;
import com.contarbn.exception.ArticoloByCodiceAlreadyExistingException;
import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.*;
import com.contarbn.repository.ArticoloRepository;
import com.contarbn.repository.ClienteArticoloRepository;
import com.contarbn.repository.GiacenzaArticoloRepository;
import com.contarbn.util.BarcodeUtils;
import com.contarbn.util.Constants;
import com.contarbn.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticoloService {

    private final ArticoloRepository articoloRepository;
    private final ArticoloImmagineService articoloImmagineService;
    private final ListinoPrezzoService listinoPrezzoService;
    private final ListinoPrezzoVariazioneService listinoPrezzoVariazioneService;
    private final GiacenzaArticoloRepository giacenzaArticoloRepository;
    private final ClienteArticoloRepository clienteArticoloRepository;

    @Autowired
    public ArticoloService(final ArticoloRepository articoloRepository, final ArticoloImmagineService articoloImmagineService,
                           final ListinoPrezzoService listinoPrezzoService, final ListinoPrezzoVariazioneService listinoPrezzoVariazioneService,
                           final GiacenzaArticoloRepository giacenzaArticoloRepository, final ClienteArticoloRepository clienteArticoloRepository){
        this.articoloRepository = articoloRepository;
        this.articoloImmagineService = articoloImmagineService;
        this.listinoPrezzoService = listinoPrezzoService;
        this.listinoPrezzoVariazioneService = listinoPrezzoVariazioneService;
        this.giacenzaArticoloRepository = giacenzaArticoloRepository;
        this.clienteArticoloRepository = clienteArticoloRepository;
    }

    public Set<Articolo> getAll(){
        log.info("Retrieving the list of 'articoli'");
        Set<Articolo> articoli = articoloRepository.findAllByOrderByCodiceAsc();
        log.info("Retrieved {} 'articoli'", articoli.size());
        return articoli;
    }

    public Set<Articolo> getAllByAttivoOrderByCodiceAsc(Boolean active){
        log.info("Retrieving the list of 'articoli' filtered by 'attivo' value '{}' ordered by codice asc", active);
        Set<Articolo> articoli = articoloRepository.findByAttivoOrderByCodiceAsc(active);
        log.info("Retrieved {} 'articoli'", articoli.size());
        return articoli;
    }

    public Set<Articolo> getAllByAttivoAndFornitoreId(Boolean active, Long idFornitore){
        log.info("Retrieving the list of 'articoli' filtered by 'attivo' value '{}' and fornitore '{}'", active, idFornitore);
        Set<Articolo> articoli = articoloRepository.findByAttivoAndFornitoreId(active,idFornitore);
        log.info("Retrieved {} 'articoli'", articoli.size());
        return articoli;
    }

    public Set<Articolo> getAllByAttivoAndBarcode(Boolean active, String barcode){
        log.info("Retrieving the list of 'articoli' filtered by 'attivo' '{}' and 'barcode' '{}'", active, barcode);
        Set<Articolo> articoli;
        articoli = articoloRepository.findByAttivoAndBarcodeEqualsAndCompleteBarcodeIsTrue(active, barcode);
        if(articoli == null || articoli.isEmpty()){
            barcode = barcode.substring(0, 7);
            articoli = articoloRepository.findByAttivoAndBarcodeStartsWithAndCompleteBarcodeIsFalse(active, barcode);
        }
        log.info("Retrieved {} 'articoli'", articoli.size());
        return articoli;
    }

    public Articolo getOne(Long articoloId){
        log.info("Retrieving 'articolo' '{}'", articoloId);
        Articolo articolo = articoloRepository.findById(articoloId).orElseThrow(ResourceNotFoundException::new);
        log.info("Retrieved 'articolo' '{}'", articolo);
        return articolo;
    }

    public Optional<Articolo> getByCodice(String codice){
        log.info("Retrieving 'articolo' with codice '{}'", codice);
        Optional<Articolo> articolo = articoloRepository.findByCodice(codice);
        if(articolo.isPresent()){
            log.info("Retrieved 'articolo' '{}'", articolo.get());
        } else {
            log.info("'articolo' with codice '{}' not existing", codice);
        }
        return articolo;
    }

    public List<Articolo> getByCodiceLike(String codice, Boolean active){
        return articoloRepository.findByCodiceLike(codice + "%", Utils.getActiveValues(active));
    }

    public Articolo create(Articolo articolo){
        log.info("Creating 'articolo'");

        checkIfArticoloByCodiceAlreadyExisting(articolo);

        String barcode = articolo.getBarcode();
        if(!StringUtils.isEmpty(barcode)){
            if(barcode.startsWith("0")){
                log.error("The barcode '"+barcode+"' is not permitted: it starts with 0");
                throw new ArticoloBarcodeCannotStartWithZeroException();
            }
        }

        String codice = articolo.getCodice().toUpperCase();
        articolo.setCodice(codice);
        articolo.setDescrizione(articolo.getDescrizione().trim());
        if(StringUtils.isNotEmpty(articolo.getDescrizione2())){
            articolo.setDescrizione2(articolo.getDescrizione2().trim());
        }
        articolo.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        String barcodeMaskLottoScadenza = articolo.getBarcodeMaskLottoScadenza();
        if(!StringUtils.isEmpty(barcodeMaskLottoScadenza)){
            articolo.setBarcodeMaskLottoScadenza(barcodeMaskLottoScadenza.toUpperCase());
        }
        handleBarcodeMask(articolo);
        articolo.setScadenzaGiorniAllarme(articolo.getScadenzaGiorniAllarme() != null ? articolo.getScadenzaGiorniAllarme() : Constants.DEFAULT_ARTICOLO_SCADENZA_GIORNI);
        Articolo createdArticolo = articoloRepository.save(articolo);

        // compute 'listini prezzi'
        log.info("Computing 'listiniPrezzi' for 'articolo' '{}'", createdArticolo.getId());
        List<ListinoPrezzo> listiniPrezzi = new ArrayList<>();
        Set<Listino> listini = listinoPrezzoService.getAll().stream().map(ListinoPrezzo::getListino).collect(Collectors.toSet());
        listini.forEach(l -> {
            ListinoPrezzo listinoPrezzo = new ListinoPrezzo();
            listinoPrezzo.setListino(l);
            listinoPrezzo.setArticolo(createdArticolo);
            listinoPrezzo.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));

            List<ListinoPrezzoVariazione> listiniPrezziVariazioni = l.getListiniPrezziVariazioni();
            String tipologiaVariazionePrezzo = listiniPrezziVariazioni.stream().filter(lpv -> lpv.getTipologiaVariazionePrezzo() != null).map(ListinoPrezzoVariazione::getTipologiaVariazionePrezzo).findFirst().orElse(null);
            Float variazionePrezzo = listiniPrezziVariazioni.stream().filter(lpv -> lpv.getVariazionePrezzo() != null).map(ListinoPrezzoVariazione::getVariazionePrezzo).findFirst().orElse(null);

            BigDecimal newPrezzo = listinoPrezzoService.computePrezzoInListinoCreation(l, createdArticolo, tipologiaVariazionePrezzo, variazionePrezzo);
            listinoPrezzo.setPrezzo(newPrezzo);

            listiniPrezzi.add(listinoPrezzo);
        });
        if(!listiniPrezzi.isEmpty()){
            listinoPrezzoService.create(listiniPrezzi);
        }
        log.info("Computed 'listiniPrezzi' for 'articolo' '{}'", createdArticolo.getId());

        log.info("Created 'articolo' '{}'", createdArticolo);
        return createdArticolo;
    }

    public Articolo update(Articolo articolo){
        log.info("Updating 'articolo'");

        checkIfArticoloByCodiceAlreadyExisting(articolo);

        String barcode = articolo.getBarcode();
        if(!StringUtils.isEmpty(barcode)){
            if(barcode.startsWith("0")){
                log.error("The barcode '"+barcode+"' is not permitted: it starts with 0");
                throw new ArticoloBarcodeCannotStartWithZeroException();
            }
        }

        Articolo articoloCurrent = articoloRepository.findById(articolo.getId()).orElseThrow(ResourceNotFoundException::new);
        BigDecimal prezzoListinoBaseCurrent = articoloCurrent.getPrezzoListinoBase();
        articolo.setDataInserimento(articoloCurrent.getDataInserimento());
        String codice = articolo.getCodice().toUpperCase();
        articolo.setCodice(codice);
        articolo.setDescrizione(articolo.getDescrizione().trim());
        if(StringUtils.isNotEmpty(articolo.getDescrizione2())){
            articolo.setDescrizione2(articolo.getDescrizione2().trim());
        }
        String barcodeMaskLottoScadenza = articolo.getBarcodeMaskLottoScadenza();
        if(!StringUtils.isEmpty(barcodeMaskLottoScadenza)){
            articolo.setBarcodeMaskLottoScadenza(barcodeMaskLottoScadenza.toUpperCase());
        }
        handleBarcodeMask(articolo);
        articolo.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));
        articolo.setScadenzaGiorniAllarme(articolo.getScadenzaGiorniAllarme() != null ? articolo.getScadenzaGiorniAllarme() : Constants.DEFAULT_ARTICOLO_SCADENZA_GIORNI);
        Articolo updatedArticolo = articoloRepository.save(articolo);

        if(updatedArticolo.getPrezzoListinoBase() != null && !updatedArticolo.getPrezzoListinoBase().equals(prezzoListinoBaseCurrent)){
            // compute 'listini prezzi'
            listinoPrezzoService.computeListiniPrezziForArticolo(updatedArticolo);
        }

        log.info("Updated 'articolo' '{}'", updatedArticolo);
        return updatedArticolo;
    }

    @Transactional
    public void delete(Long articoloId){
        log.info("Deleting 'listiniPrezziVariazioni' of articolo '{}'", articoloId);
        listinoPrezzoVariazioneService.deleteByArticoloId(articoloId);
        log.info("Deleted 'listiniPrezziVariazioni' of articolo '{}'", articoloId);

        log.info("Deleting 'listiniPrezzi' of articolo '{}'", articoloId);
        listinoPrezzoService.deleteByArticoloId(articoloId);
        log.info("Deleted 'listiniPrezzi' of articolo '{}'", articoloId);

        log.info("Deleting 'giacenze' of articolo '{}'", articoloId);
        giacenzaArticoloRepository.deleteByArticoloId(articoloId);
        log.info("Deleted 'giacenze' of articolo '{}'", articoloId);

        log.info("Deleting 'cliente-articolo' of articolo '{}'", articoloId);
        clienteArticoloRepository.deleteByArticoloId(articoloId);
        log.info("Deleted 'cliente-articolo' of articolo '{}'", articoloId);

        log.info("Deleting 'articolo' '{}'", articoloId);
        articoloImmagineService.deleteByArticoloId(articoloId);
        articoloRepository.deleteById(articoloId);
        log.info("Deleted 'articolo' '{}'", articoloId);
    }

    public List<ArticoloImmagine> getArticoloImmagini(Long articoloId){
        log.info("Retrieving the list of 'articoloImmagini' of the 'articolo' '{}'", articoloId);
        List<ArticoloImmagine> articoloImmagini = articoloImmagineService.getByArticoloId(articoloId);
        log.info("Retrieved {} 'articoloImmagini'", articoloImmagini.size());
        return articoloImmagini;
    }

    public List<ListinoPrezzo> getArticoloListiniPrezzi(Long articoloId){
        log.info("Retrieving the list of 'listiniPrezzi' of the 'articolo' '{}'", articoloId);
        List<ListinoPrezzo> listiniPrezzi = listinoPrezzoService.getByArticoloId(articoloId);
        log.info("Retrieved {} 'listiniPrezzi'", listiniPrezzi.size());
        return listiniPrezzi;
    }

    private void handleBarcodeMask(Articolo articolo){
        if(!StringUtils.isEmpty(articolo.getBarcodeMaskLottoScadenza())){
            articolo.setBarcodeRegexpLotto(BarcodeUtils.createRegexpLotto(articolo.getBarcodeMaskLottoScadenza()));
            articolo.setBarcodeRegexpDataScadenza(BarcodeUtils.createRegexpDataScadenza(articolo.getBarcodeMaskLottoScadenza()));
        }
    }

    private void checkIfArticoloByCodiceAlreadyExisting(Articolo articolo){
        Optional<Articolo> articoloByCodice = getByCodice(articolo.getCodice());
        if(articoloByCodice.isPresent()){
            if(articolo.getId() != null){
                if(!articolo.getId().equals(articoloByCodice.get().getId())){
                    throw new ArticoloByCodiceAlreadyExistingException(articolo.getCodice());
                }
            } else {
                throw new ArticoloByCodiceAlreadyExistingException(articolo.getCodice());
            }
        }
    }
}

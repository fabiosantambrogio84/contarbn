package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.*;
import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VProduzione;
import com.contarbn.model.views.VProduzioneConfezioneEtichetta;
import com.contarbn.repository.ProduzioneRepository;
import com.contarbn.repository.RicettaRepository;
import com.contarbn.repository.views.VProduzioneConfezioneEtichettaRepository;
import com.contarbn.repository.views.VProduzioneRepository;
import com.contarbn.util.BarcodeUtils;
import com.contarbn.util.Constants;
import com.contarbn.util.LottoUtils;
import com.contarbn.util.Utils;
import com.contarbn.util.enumeration.Operation;
import com.contarbn.util.enumeration.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProduzioneService {

    private final ProduzioneRepository produzioneRepository;
    private final VProduzioneRepository vProduzioneRepository;
    private final VProduzioneConfezioneEtichettaRepository vProduzioneConfezioneEtichettaRepository;
    private final ProduzioneIngredienteService produzioneIngredienteService;
    private final ProduzioneConfezioneService produzioneConfezioneService;
    private final GiacenzaArticoloService giacenzaArticoloService;
    private final GiacenzaIngredienteService giacenzaIngredienteService;
    private final ArticoloService articoloService;
    private final IngredienteService ingredienteService;
    private final FornitoreService fornitoreService;
    private final ConfezioneService confezioneService;
    private final UnitaMisuraService unitaMisuraService;
    private final AliquotaIvaService aliquotaIvaService;
    private final RicettaRepository ricettaRepository;

    public List<VProduzione> getAllByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, Integer codice, String ricetta, String barcodeEan13, String barcodeEan128){
        log.info("Retrieving the list of 'produzioni' filtered by request parameters");
        List<VProduzione> produzioni = vProduzioneRepository.findByFilters(draw, start, length, sortOrders, codice, ricetta, barcodeEan13, barcodeEan128);
        log.info("Retrieved {} 'produzioni'", produzioni.size());
        return produzioni;
    }

    public Integer getCountByFilters(Integer codice, String ricetta, String barcodeEan13, String barcodeEan128){
        log.info("Retrieving the count of 'produzioni' filtered by request parameters");
        Integer count = vProduzioneRepository.countByFilters(codice, ricetta, barcodeEan13, barcodeEan128);
        log.info("Retrieved {} 'produzioni'", count);
        return count;
    }

    public Set<VProduzione> getAllByLotto(String lotto){
        log.info("Retrieving the list of 'produzioni' filtered by 'lotto' '{}'", lotto);
        Set<VProduzione> produzioni = vProduzioneRepository.findAllByLotto(lotto);
        log.info("Retrieved {} 'produzioni'", produzioni.size());
        return produzioni;
    }

    public Produzione getOne(Long produzioneId){
        log.info("Retrieving 'produzione' '{}'", produzioneId);
        Produzione produzione = produzioneRepository.findById(produzioneId).orElseThrow(ResourceNotFoundException::new);
        log.info("Retrieved 'produzione' '{}'", produzione);
        return produzione;
    }

    public Articolo checkArticolo(String codiceRicetta, Long idConfezione){
        Confezione confezione = confezioneService.getOne(idConfezione);
        Ricetta ricetta = new Ricetta();
        ricetta.setCodice(codiceRicetta);
        String codiceArticolo = createCodiceArticolo(ricetta, confezione);
        return articoloService.getByCodice(codiceArticolo).orElse(null);
    }

    @Transactional
    public Produzione create(Produzione produzione){
        log.info("Creating 'produzione'");

        produzione.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));

        Produzione createdProduzione = produzioneRepository.save(produzione);
        String tipologia = createdProduzione.getTipologia();
        Long produzioneId = createdProduzione.getId();
        Date dataProduzione = createdProduzione.getDataProduzione();
        Date scadenzaProduzione = createdProduzione.getScadenza();
        Long idRicetta = createdProduzione.getRicetta().getId();

        if(produzione.getScopo().equalsIgnoreCase("vendita")){
            LocalDate localDate = dataProduzione != null ? dataProduzione.toLocalDate() : LocalDate.now();
            Integer anno = localDate.getYear();

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("uu");
            String yearTwoDigits = dateTimeFormatter.format(localDate);
            Integer giorno = localDate.getDayOfYear();
            Integer codice = produzioneRepository.findNextCodiceByLottoAnno(anno).orElse(1);
            String lotto = LottoUtils.createLottoProduzione(yearTwoDigits, codice);

            log.info("Anno {}, codice {}, lotto {}", anno, codice, lotto);

            createdProduzione.setCodice(codice);
            createdProduzione.setLottoAnno(anno);
            createdProduzione.setLottoGiorno(giorno);
            createdProduzione.setLottoNumeroProgressivo(codice);
            createdProduzione.setLotto(lotto);
        }
        final String createdLotto = createdProduzione.getLotto();
        final double quantitaTotale = createdProduzione.getProduzioneIngredienti().stream().mapToDouble(ProduzioneIngrediente::getQuantita).sum();

        createdProduzione.getProduzioneIngredienti().forEach(pi -> {
            pi.setPercentuale(Utils.computePercentuale(pi.getQuantita(), (float) quantitaTotale));
            pi.getId().setProduzioneId(produzioneId);
            pi.getId().setUuid(UUID.randomUUID().toString());

            produzioneIngredienteService.create(pi);

            // compute 'giacenza ingrediente'
            giacenzaIngredienteService.computeGiacenza(pi.getId().getIngredienteId(), pi.getLotto(), pi.getScadenza(), pi.getQuantita(), Resource.PRODUZIONE_INGREDIENTE);
        });

        createdProduzione.getProduzioneConfezioni().forEach(pc -> {
            Confezione confezione = confezioneService.getOne(pc.getConfezione().getId());
            pc.setConfezione(confezione);

            Produzione produzioneTmp = new Produzione();
            produzioneTmp.setId(produzioneId);
            pc.setProduzione(produzioneTmp);
            pc.setLottoProduzione(createdLotto);

            Long id;
            float quantita = 0f;
            if(tipologia.equals("SCORTA")){

                // create, or retrieve, the associated Ingrediente
                Ingrediente ingrediente = getOrCreateIngrediente(confezione, idRicetta);
                pc.setIngrediente(ingrediente);
                id = ingrediente.getId();

                if(confezione.getPeso() != null){
                    BigDecimal quantitaBd = BigDecimal.valueOf(confezione.getPeso() / 1000);
                    quantitaBd = Utils.roundQuantity(quantitaBd);
                    quantita = quantitaBd.floatValue() * pc.getNumConfezioniProdotte();
                }

            } else {
                // create, or retrieve, the associated Articolo
                Articolo articolo = getOrCreateArticolo(pc, idRicetta, dataProduzione);
                pc.setArticolo(articolo);
                id = articolo.getId();
                quantita = pc.getNumConfezioniProdotte().floatValue();
            }

            produzioneConfezioneService.create(pc);

            if(tipologia.equals("SCORTA")){
                // compute 'giacenza ingrediente'
                giacenzaIngredienteService.computeGiacenza(id, createdLotto, scadenzaProduzione, quantita, Resource.PRODUZIONE_SCORTA);
            } else {
                // compute 'giacenza articolo'
                giacenzaArticoloService.computeGiacenza(id, createdLotto, scadenzaProduzione);
            }

        });
        Integer numeroConfezioni = createdProduzione.getProduzioneConfezioni().stream().mapToInt(ProduzioneConfezione::getNumConfezioni).sum();

        createdProduzione.setNumeroConfezioni(numeroConfezioni);

        String barcodeEan128 = BarcodeUtils.createBarcodeEan128(createdProduzione);
        if(StringUtils.isNotEmpty(barcodeEan128)){
            createdProduzione.setBarcodeEan128(barcodeEan128);
        }

        createdProduzione = produzioneRepository.save(createdProduzione);

        log.info("Created 'produzione' '{}'", createdProduzione);
        return createdProduzione;
    }

    /*
    @Transactional
    public Produzione update(Produzione produzione){
        log.info("Updating 'produzione'");
        Set<ProduzioneIngrediente> produzioneIngredienti = produzione.getProduzioneIngredienti();
        produzione.setProduzioneIngredienti(new HashSet<>());
        produzioneIngredienteService.deleteByProduzioneId(produzione.getId());

        Set<ProduzioneConfezione> produzioneConfezioni = produzione.getProduzioneConfezioni();
        produzione.setProduzioneConfezioni(new HashSet<>());
        produzioneConfezioneService.deleteByProduzioneId(produzione.getId());

        Produzione produzioneCurrente = produzioneRepository.findById(produzione.getId()).orElseThrow(ResourceNotFoundException::new);
        produzione.setDataInserimento(produzioneCurrente.getDataInserimento());
        produzione.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));

        Produzione updatedProduzione = produzioneRepository.save(produzione);
        Long produzioneId = updatedProduzione.getId();

        produzioneIngredienti.stream().forEach(pi -> {
            pi.getId().setProduzioneId(produzioneId);
            pi.getId().setUuid(UUID.randomUUID().toString());
            produzioneIngredienteService.create(pi);

            // compute 'giacenza ingrediente'
            giacenzaIngredienteService.computeGiacenza(pi.getId().getIngredienteId(), pi.getLotto(), pi.getScadenza(), pi.getQuantita(), Resource.PRODUZIONE_INGREDIENTE);
        });
        produzioneConfezioni.stream().forEach(pc -> {
            pc.getId().setProduzioneId(produzioneId);
            produzioneConfezioneService.create(pc);
        });
        Integer numeroConfezioni = updatedProduzione.getProduzioneConfezioni().stream().collect(Collectors.summingInt(ProduzioneConfezione::getNumConfezioni));

        updatedProduzione.setCodice(produzioneId.intValue());
        updatedProduzione.setNumeroConfezioni(numeroConfezioni);
        updatedProduzione = produzioneRepository.save(produzione);

        // create 'articolo', if not exists
        log.info("Creating 'articolo' if not already exists...");
        Fornitore fornitore = fornitoreService.getByRagioneSociale(Constants.DEFAULT_FORNITORE);
        String codiceArticolo = Constants.DEFAULT_FORNITORE_INITIALS + updatedProduzione.getRicetta().getCodice();
        Optional<Articolo> optionalArticolo = articoloService.getByCodice(codiceArticolo);
        Articolo articolo;
        if(!optionalArticolo.isPresent()){
            articolo = new Articolo();
            articolo.setCodice(codiceArticolo);
            articolo.setDescrizione(updatedProduzione.getRicetta().getNome());
            articolo.setFornitore(fornitore);
            articolo.setData(updatedProduzione.getDataProduzione());
            articolo.setQuantitaPredefinita(updatedProduzione.getQuantitaTotale());
            articolo.setSitoWeb(Boolean.FALSE);
            articolo.setAttivo(Boolean.TRUE);
            articolo = articoloService.create(articolo);
            log.info("Created 'articolo' '{}' from produzione", articolo);
        } else {
            log.info("The 'articolo' with 'codice' '{}' already exists", codiceArticolo);
            articolo = optionalArticolo.get();
        }

        // compute 'giacenza articolo'
        giacenzaArticoloService.computeGiacenza(articolo.getId(), updatedProduzione.getLotto(), updatedProduzione.getScadenza(), updatedProduzione.getQuantitaTotale(), Resource.PRODUZIONE);

        log.info("Updated 'produzione' '{}'", updatedProduzione);
        return updatedProduzione;
    }
     */

    @Transactional
    public void delete(Long produzioneId){
        log.info("Deleting 'produzione' '{}'", produzioneId);

        Produzione produzione = produzioneRepository.findById(produzioneId).orElseThrow(ResourceNotFoundException::new);
        String tipologia = produzione.getTipologia();

        Set<ProduzioneConfezione> produzioneConfezioni = produzioneConfezioneService.findByProduzioneId(produzioneId);
        Set<ProduzioneIngrediente> produzioneIngredienti = produzioneIngredienteService.findByProduzioneId(produzioneId);

        produzioneIngredienteService.deleteByProduzioneId(produzioneId);
        produzioneConfezioneService.deleteByProduzioneId(produzioneId);
        produzioneRepository.deleteById(produzioneId);

        if(produzioneConfezioni != null && !produzioneConfezioni.isEmpty()){

            Fornitore defaultFornitore = fornitoreService.getByRagioneSociale(Constants.DEFAULT_FORNITORE);

            for(ProduzioneConfezione produzioneConfezione : produzioneConfezioni){
                Ricetta ricetta = ricettaRepository.findById(produzione.getRicetta().getId()).orElse(null);
                Confezione confezione = confezioneService.getOne(produzioneConfezione.getConfezione().getId());

                if(tipologia.equals("SCORTA")){
                    String codiceIngrediente = createCodiceIngrediente(ricetta, confezione);
                    Optional<Ingrediente> optionalIngrediente = ingredienteService.getByCodice(codiceIngrediente);
                    if(optionalIngrediente.isPresent()){
                        Ingrediente ingrediente = optionalIngrediente.get();

                        float quantita = 0f;
                        if(confezione.getPeso() != 0){
                            BigDecimal quantitaBd = BigDecimal.valueOf(confezione.getPeso() / 1000);
                            quantitaBd = Utils.roundQuantity(quantitaBd);
                            quantita = quantitaBd.floatValue() * produzioneConfezione.getNumConfezioniProdotte();
                        }

                        giacenzaIngredienteService.computeGiacenza(ingrediente.getId(), produzioneConfezione.getLotto(), produzione.getScadenza(), (quantita *-1 ), Resource.PRODUZIONE_SCORTA);
                    }

                } else {
                    Articolo articolo = produzioneConfezione.getArticolo();
                    if(articolo != null){

                        giacenzaArticoloService.createMovimentazioneManualeArticolo(articolo.getId(), produzioneConfezione.getLotto(), produzione.getScadenza(), produzioneConfezione.getNumConfezioniProdotte(), produzione.getQuantitaTotale(), Operation.DELETE, Resource.PRODUZIONE, produzione.getId(), String.valueOf(produzione.getCodice()), null, (defaultFornitore != null ? defaultFornitore.getRagioneSociale() : null));

                        giacenzaArticoloService.computeGiacenza(articolo.getId(), produzioneConfezione.getLotto(), produzione.getScadenza());
                    }
                }
            }
        }

        if(produzioneIngredienti != null && !produzioneIngredienti.isEmpty()){
            for(ProduzioneIngrediente produzioneIngrediente : produzioneIngredienti){
                giacenzaIngredienteService.computeGiacenza(produzioneIngrediente.getId().getIngredienteId(), produzioneIngrediente.getLotto(), produzioneIngrediente.getScadenza(), produzioneIngrediente.getQuantita()*-1, Resource.PRODUZIONE_INGREDIENTE);
            }
        }

        log.info("Deleted 'produzione' '{}'", produzioneId);
    }

    @Transactional
    public void deleteByRicettaId(Long idRicetta){
        log.info("Deleting 'produzioni' of 'ricetta' '{}'", idRicetta);
        List<Produzione> produzioniToDelete = produzioneRepository.findByRicettaId(idRicetta);
        produzioniToDelete.forEach(p -> delete(p.getId()));
        log.info("Deleted 'produzioni' of 'ricetta' '{}'", idRicetta);
    }

    public Set<ProduzioneConfezione> getProduzioneConfezioni(Long produzioneId){
        log.info("Retrieving 'produzioneConfezioni' for produzione '{}'", produzioneId);
        Set<ProduzioneConfezione> produzioneConfezioni = produzioneConfezioneService.findByProduzioneId(produzioneId);
        log.info("Retrieved {} 'produzioneConfezioni'", produzioneConfezioni.size());
        return produzioneConfezioni;
    }

    public VProduzioneConfezioneEtichetta getProduzioneConfezioneEtichetta(Long idProduzioneConfezione){
        log.info("Retrieving data for 'etichetta' of 'produzione-confezione' '{}'", idProduzioneConfezione);
        VProduzioneConfezioneEtichetta vProduzioneConfezioneEtichetta = vProduzioneConfezioneEtichettaRepository.findById(idProduzioneConfezione).orElseThrow(ResourceNotFoundException::new);
        log.info("Retrieved data for 'etichetta' of 'produzione-confezione' '{}'", vProduzioneConfezioneEtichetta);
        return vProduzioneConfezioneEtichetta;
    }

    private Articolo getOrCreateArticolo(ProduzioneConfezione produzioneConfezione, Long idRicetta, Date dataProduzione){
        log.info("Creating or retrieving associated 'articolo'...");

        Articolo articolo = produzioneConfezione.getArticolo();
        if(articolo != null){
            return articoloService.getOne(articolo.getId());
        }

        Ricetta ricetta = ricettaRepository.findById(idRicetta).orElse(null);

        Fornitore fornitore = fornitoreService.getByRagioneSociale(Constants.DEFAULT_FORNITORE);

        Confezione confezione = confezioneService.getOne(produzioneConfezione.getConfezione().getId());

        String codiceArticolo = createCodiceArticolo(ricetta, confezione);

        articolo = new Articolo();
        articolo.setCodice(codiceArticolo);
        articolo.setDescrizione(createDescrizioneArticolo(ricetta, confezione));
        articolo.setBarcode(produzioneConfezione.getBarcode());
        articolo.setFornitore(fornitore);
        articolo.setData(dataProduzione);
        articolo.setQuantitaPredefinita(1f);
        articolo.setUnitaMisura(unitaMisuraService.getByNome("pz"));
        articolo.setSitoWeb(Boolean.FALSE);
        articolo.setAttivo(Boolean.TRUE);
        articolo = articoloService.create(articolo);
        log.info("Created 'articolo' '{}' from produzione", articolo);

        return articolo;
    }

    private Ingrediente getOrCreateIngrediente(Confezione confezione, Long idRicetta){
        log.info("Creating or retrieving associated 'ingrediente'...");

        // retrieve Ricetta
        Ricetta ricetta = ricettaRepository.findById(idRicetta).orElse(null);

        // retrieve default Fornitore
        Fornitore fornitore = fornitoreService.getByRagioneSociale(Constants.DEFAULT_FORNITORE);

        String codiceIngrediente = createCodiceIngrediente(ricetta, confezione);

        Optional<Ingrediente> optionalIngrediente = ingredienteService.getByCodice(codiceIngrediente);
        Ingrediente ingrediente;

        if(!optionalIngrediente.isPresent()){
            ingrediente = new Ingrediente();
            ingrediente.setCodice(codiceIngrediente);
            ingrediente.setDescrizione(createDescrizioneIngrediente(ricetta, confezione));
            ingrediente.setFornitore(fornitore);
            ingrediente.setPrezzo(createPrezzoIngredienteScorta(ricetta));
            ingrediente.setUnitaMisura(unitaMisuraService.getByNome("kg"));
            ingrediente.setAliquotaIva(aliquotaIvaService.getOne(2L)); // 10%
            ingrediente.setAttivo(Boolean.TRUE);
            ingrediente = ingredienteService.create(ingrediente);
            log.info("Created 'ingrediente' '{}' from produzione", ingrediente);
        } else {
            log.info("Retrieved 'ingrediente' with 'codice' '{}'", codiceIngrediente);
            ingrediente = optionalIngrediente.get();
        }

        return ingrediente;
    }

    private String createCodiceArticolo(Ricetta ricetta, Confezione confezione){
        float pesoConfezione = confezione.getPeso()/1000;
        String peso = Float.toString(pesoConfezione).replace(".", ",");
        if(peso.contains(",0")){
            peso = StringUtils.substringBefore(peso,",");
        }

        return Constants.DEFAULT_FORNITORE_INITIALS + (ricetta != null ? ricetta.getCodice() : "")+peso;
    }

    private String createCodiceIngrediente(Ricetta ricetta, Confezione confezione){
        float pesoConfezione = confezione.getPeso()/1000;
        String peso = Float.toString(pesoConfezione).replace(".", ",");
        if(peso.contains(",0")){
            peso = StringUtils.substringBefore(peso,",");
        }

        return Constants.DEFAULT_FORNITORE_INITIALS + (ricetta != null ? ricetta.getCodice() : "")+peso;
    }

    private String createDescrizioneArticolo(Ricetta ricetta, Confezione confezione){
        float pesoConfezione = confezione.getPeso()/1000;
        String peso = Float.toString(pesoConfezione).replace(".", ",");
        if(peso.contains(",0")){
            peso = StringUtils.substringBefore(peso,",");
        }

        return ricetta.getNome()+" "+peso+"kg";
    }

    private String createDescrizioneIngrediente(Ricetta ricetta, Confezione confezione){
        float pesoConfezione = confezione.getPeso()/1000;
        String peso = Float.toString(pesoConfezione).replace(".", ",");
        if(peso.contains(",0")){
            peso = StringUtils.substringBefore(peso,",");
        }

        return ricetta.getNome()+" "+peso+"kg";
    }

    private BigDecimal createPrezzoIngredienteScorta(Ricetta ricetta){
        BigDecimal prezzo = new BigDecimal(1);
        BigDecimal costoTotale = ricetta.getCostoTotale();
        float pesoTotale = ricetta.getPesoTotale() != null ? ricetta.getPesoTotale() : 1f;

        if(costoTotale != null){
            prezzo = costoTotale.divide(BigDecimal.valueOf(pesoTotale));
        }
        return prezzo;
    }

}

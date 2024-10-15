package com.contarbn.service;

import com.contarbn.model.*;
import com.contarbn.repository.*;
import com.contarbn.util.Constants;
import com.contarbn.util.Utils;
import com.contarbn.util.enumeration.Operation;
import com.contarbn.util.enumeration.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MovimentazioneService {

    private static final String INPUT = "INPUT";
    private static final String OUTPUT = "OUTPUT";
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

    private final DdtAcquistoArticoloService ddtAcquistoArticoloService;
    private final DdtAcquistoIngredienteService ddtAcquistoIngredienteService;
    private final DdtArticoloService ddtArticoloService;
    private final FatturaAccompagnatoriaArticoloService fatturaAccompagnatoriaArticoloService;
    private final RicevutaPrivatoArticoloService ricevutaPrivatoArticoloService;
    private final ProduzioneIngredienteService produzioneIngredienteService;
    private final FatturaAccompagnatoriaAcquistoArticoloService fatturaAccompagnatoriaAcquistoArticoloService;
    private final FornitoreService fornitoreService;
    private final ArticoloRepository articoloRepository;
    private final IngredienteRepository ingredienteRepository;
    private final DdtAcquistoRepository ddtAcquistoRepository;
    private final DdtRepository ddtRepository;
    private final FatturaAccompagnatoriaRepository fatturaAccompagnatoriaRepository;
    private final RicevutaPrivatoRepository ricevutaPrivatoRepository;
    private final ProduzioneRepository produzioneRepository;
    private final ProduzioneConfezioneRepository produzioneConfezioneRepository;
    private final FatturaAccompagnatoriaAcquistoRepository fatturaAccompagnatoriaAcquistoRepository;
    private final MovimentazioneManualeArticoloService movimentazioneManualeArticoloService;
    private final MovimentazioneManualeIngredienteService movimentazioneManualeIngredienteService;
    private final ConfezioneService confezioneService;

    public Set<Movimentazione> getMovimentazioniArticolo(Long idArticolo, String lotto, Date scadenza, Timestamp dataAggiornamento){
        log.info("Retrieving 'movimentazioni' of articolo {}, lotto {}, scadenza {}, data_aggiornamento {}", idArticolo, lotto, scadenza, dataAggiornamento);

        Set<Movimentazione> movimentazioni = new HashSet<>();

        Set<DdtArticolo> ddtArticoli;
        Set<FatturaAccompagnatoriaArticolo> fatturaAccompagnatoriaArticoli;
        Set<RicevutaPrivatoArticolo> ricevutaPrivatoArticoli;
        Set<DdtAcquistoArticolo> ddtAcquistoArticoli;
        Set<FatturaAccompagnatoriaAcquistoArticolo> fatturaAccompagnatoriaAcquistoArticoli;
        Set<ProduzioneConfezione> produzioneConfezioni;
        Set<MovimentazioneManualeArticolo> movimentazioniManualiArticoli;

        Articolo articolo = articoloRepository.findById(idArticolo).get();

        ddtArticoli = ddtArticoloService.getByArticoloIdAndLottoAndScadenza(articolo.getId(), lotto, scadenza, dataAggiornamento);

        fatturaAccompagnatoriaArticoli = fatturaAccompagnatoriaArticoloService.getByArticoloIdAndLottoAndScadenza(articolo.getId(), lotto, scadenza, dataAggiornamento);

        ricevutaPrivatoArticoli = ricevutaPrivatoArticoloService.getByArticoloIdAndLottoAndScadenza(articolo.getId(), lotto, scadenza, dataAggiornamento);

        ddtAcquistoArticoli = ddtAcquistoArticoloService.getByArticoloIdAndLottoAndScadenza(articolo.getId(), lotto, scadenza, dataAggiornamento);

        produzioneConfezioni = produzioneConfezioneRepository.findByArticoloIdAndLottoProduzione(articolo.getId(), lotto);
        if(produzioneConfezioni != null && !produzioneConfezioni.isEmpty()){
            Set<Long> produzioniIds = new HashSet<>();
            produzioneConfezioni.forEach(pc -> {
                Produzione produzione = produzioneRepository.findById(pc.getProduzione().getId()).get();
                if(scadenza != null){
                    if(produzione.getScadenza() != null && produzione.getScadenza().toLocalDate().compareTo(scadenza.toLocalDate())==0){
                        if(dataAggiornamento != null){
                            if(produzione.getDataAggiornamento().after(dataAggiornamento)){
                                produzioniIds.add(produzione.getId());
                            }
                        } else {
                            produzioniIds.add(produzione.getId());
                        }
                    }
                } else {
                    if(dataAggiornamento != null){
                        if(produzione.getDataAggiornamento().after(dataAggiornamento)){
                            produzioniIds.add(produzione.getId());
                        }
                    } else {
                        produzioniIds.add(produzione.getId());
                    }
                }
            });
            produzioneConfezioni = produzioneConfezioni.stream().filter(pc -> (produzioniIds.contains(pc.getProduzione().getId()))).collect(Collectors.toSet());
        }

        fatturaAccompagnatoriaAcquistoArticoli = fatturaAccompagnatoriaAcquistoArticoloService.getByArticoloIdAndLottoAndScadenza(articolo.getId(), lotto, scadenza, dataAggiornamento);

        movimentazioniManualiArticoli = movimentazioneManualeArticoloService.getByArticoloIdAndLottoAndScadenza(articolo.getId(), lotto, scadenza, dataAggiornamento);

        if(ddtArticoli != null && !ddtArticoli.isEmpty()){
            ddtArticoli.forEach(da -> movimentazioni.add(create(da, lotto, scadenza, articolo)));
        }

        if(fatturaAccompagnatoriaArticoli != null && !fatturaAccompagnatoriaArticoli.isEmpty()){
            fatturaAccompagnatoriaArticoli.forEach(faa -> movimentazioni.add(create(faa, lotto, scadenza, articolo)));
        }

        if(ricevutaPrivatoArticoli != null && !ricevutaPrivatoArticoli.isEmpty()){
            ricevutaPrivatoArticoli.forEach(rpa -> movimentazioni.add(create(rpa, lotto, scadenza, articolo)));
        }

        if(ddtAcquistoArticoli != null && !ddtAcquistoArticoli.isEmpty()){
            ddtAcquistoArticoli.forEach(daa -> movimentazioni.add(create(daa, lotto, scadenza, articolo)));
        }

        if(produzioneConfezioni != null && !produzioneConfezioni.isEmpty()){
            produzioneConfezioni.forEach(pc -> movimentazioni.add(create(pc, lotto, scadenza)));
        }

        if(fatturaAccompagnatoriaAcquistoArticoli != null && !fatturaAccompagnatoriaAcquistoArticoli.isEmpty()){
            fatturaAccompagnatoriaAcquistoArticoli.forEach(faaa -> movimentazioni.add(create(faaa, lotto, scadenza, articolo)));
        }

        if(movimentazioniManualiArticoli != null && !movimentazioniManualiArticoli.isEmpty()){
            movimentazioniManualiArticoli.forEach(mma -> movimentazioni.add(create(mma, lotto, articolo)));
        }

        log.info("Retrieved '{}' 'movimentazioni' of articolo {}, lotto {}, scadenza {}",  movimentazioni.size(), idArticolo, lotto, scadenza);
        return movimentazioni;
    }

    public Set<Movimentazione> getMovimentazioniIngrediente(GiacenzaIngrediente giacenzaIngrediente){
        log.info("Retrieving 'movimentazioni' of 'giacenza ingrediente' '{}'", giacenzaIngrediente.getId());
        Set<Movimentazione> movimentazioni = new HashSet<>();
        Set<DdtAcquistoIngrediente> ddtAcquistoIngredienti;
        Set<ProduzioneIngrediente> produzioneIngredienti;
        Set<ProduzioneConfezione> produzioneConfezioni;
        Set<MovimentazioneManualeIngrediente> movimentazioniManualiIngredienti;

        Ingrediente ingrediente = ingredienteRepository.findById(giacenzaIngrediente.getIngrediente().getId()).get();

        // retrieve the set of 'DdtAcquistoIngrediente'
        ddtAcquistoIngredienti = ddtAcquistoIngredienteService.getByIngredienteIdAndLottoAndScadenza(ingrediente.getId(), giacenzaIngrediente.getLotto(), giacenzaIngrediente.getScadenza());

        // retrieve the set of 'ProduzioneIngrediente'
        produzioneIngredienti = produzioneIngredienteService.getByIngredienteIdAndLottoAndScadenza(ingrediente.getId(), giacenzaIngrediente.getLotto(), giacenzaIngrediente.getScadenza());

        // retrieve the set of 'ProduzioniConfezioni'
        produzioneConfezioni = produzioneConfezioneRepository.findByIngredienteIdAndLottoProduzione(ingrediente.getId(), giacenzaIngrediente.getLotto());
        if(produzioneConfezioni != null && !produzioneConfezioni.isEmpty()){
            Set<Long> produzioniIds = new HashSet<>();
            produzioneConfezioni.forEach(pc -> {
                Produzione produzione = produzioneRepository.findById(pc.getProduzione().getId()).get();
                if(giacenzaIngrediente.getScadenza() != null){
                    if(produzione.getScadenza() != null && produzione.getScadenza().toLocalDate().compareTo(giacenzaIngrediente.getScadenza().toLocalDate())==0){
                        produzioniIds.add(produzione.getId());
                    }
                } else {
                    produzioniIds.add(produzione.getId());
                }
            });
            produzioneConfezioni = produzioneConfezioni.stream().filter(pc -> (produzioniIds.contains(pc.getProduzione().getId()))).collect(Collectors.toSet());
        }

        // retrieve the set of 'MovimentazioneManualeIngrediente'
        movimentazioniManualiIngredienti = movimentazioneManualeIngredienteService.getByIngredienteIdAndLottoAndScadenza(ingrediente.getId(), giacenzaIngrediente.getLotto(), giacenzaIngrediente.getScadenza());

        // Create 'movimentazione' from 'DdtAcquistoIngrediente'
        if(ddtAcquistoIngredienti != null && !ddtAcquistoIngredienti.isEmpty()){
            ddtAcquistoIngredienti.forEach(dai -> {
                Movimentazione movimentazione = new Movimentazione();
                movimentazione.setIdGiacenza(giacenzaIngrediente.getId());
                movimentazione.setInputOutput(INPUT);
                if(dai.getDdtAcquisto() != null){
                    movimentazione.setData(dai.getDdtAcquisto().getData());
                }
                movimentazione.setQuantita(dai.getQuantita());
                movimentazione.setDescrizione(createDescrizione(dai, giacenzaIngrediente, ingrediente));

                movimentazioni.add(movimentazione);
            });
        }

        // Create 'movimentazione' from 'ProduzioneIngrediente'
        if(produzioneIngredienti != null && !produzioneIngredienti.isEmpty()){
            produzioneIngredienti.forEach(pi -> {
                Produzione produzione = produzioneRepository.findById(pi.getId().getProduzioneId()).get();

                Movimentazione movimentazione = new Movimentazione();
                movimentazione.setIdGiacenza(giacenzaIngrediente.getId());
                movimentazione.setInputOutput(OUTPUT);
                movimentazione.setData(produzione.getDataProduzione());
                movimentazione.setQuantita(pi.getQuantita());
                movimentazione.setDescrizione(createDescrizione(produzione, giacenzaIngrediente.getLotto(), giacenzaIngrediente.getScadenza(), pi.getQuantita()));

                movimentazioni.add(movimentazione);
            });
        }

        // Create 'movimentazione' from 'ProduzioneConfezione'
        if(produzioneConfezioni != null && !produzioneConfezioni.isEmpty()){
            produzioneConfezioni.forEach(pc -> {
                Produzione produzione = produzioneRepository.findById(pc.getProduzione().getId()).get();
                Confezione confezione = confezioneService.getOne(pc.getConfezione().getId());

                float quantita = 0f;
                if(confezione.getPeso() != null){
                    BigDecimal quantitaBd = BigDecimal.valueOf(confezione.getPeso() / 1000);
                    quantitaBd = Utils.roundQuantity(quantitaBd);
                    quantita = quantitaBd.floatValue() * pc.getNumConfezioniProdotte();
                }

                Movimentazione movimentazione = new Movimentazione();
                movimentazione.setIdGiacenza(giacenzaIngrediente.getId());
                movimentazione.setInputOutput(INPUT);
                movimentazione.setData(produzione.getDataProduzione());
                movimentazione.setQuantita(quantita);
                movimentazione.setDescrizione(createDescrizione(produzione, giacenzaIngrediente.getLotto(), giacenzaIngrediente.getScadenza(), quantita));

                movimentazioni.add(movimentazione);
            });
        }

        // Create 'movimentazione' for 'MovimentazioneManualeIngrediente'
        if(movimentazioniManualiIngredienti != null && !movimentazioniManualiIngredienti.isEmpty()){
            movimentazioniManualiIngredienti.forEach(mmi -> {
                Movimentazione movimentazione = new Movimentazione();
                movimentazione.setIdGiacenza(giacenzaIngrediente.getId());
                movimentazione.setInputOutput(INPUT);
                movimentazione.setData(new Date(mmi.getDataInserimento().getTime()));
                movimentazione.setQuantita(mmi.getQuantita());
                movimentazione.setDescrizione(createDescrizione(mmi, giacenzaIngrediente));

                movimentazioni.add(movimentazione);
            });
        }

        log.info("Retrieved '{}' 'movimentazioni' for 'giacenza ingrediente' '{}'", movimentazioni.size(), giacenzaIngrediente.getId());
        return movimentazioni;
    }

    private Movimentazione create(DdtArticolo ddtArticolo, String lotto, Date scadenza, Articolo articolo){
        Movimentazione movimentazione = new Movimentazione();
        //movimentazione.setIdGiacenza(giacenzaArticolo.getId());
        movimentazione.setInputOutput(OUTPUT);
        if(ddtArticolo.getDdt() != null){
            movimentazione.setData(ddtArticolo.getDdt().getData());
        }
        movimentazione.setPezzi(ddtArticolo.getNumeroPezzi());
        movimentazione.setQuantita(ddtArticolo.getQuantita());
        movimentazione.setDescrizione(createDescrizione(ddtArticolo, lotto, scadenza, articolo));
        movimentazione.setDataInserimento(ddtArticolo.getDataInserimento());

        return movimentazione;
    }

    private Movimentazione create(FatturaAccompagnatoriaArticolo fatturaAccompagnatoriaArticolo, String lotto, Date scadenza, Articolo articolo){
        Movimentazione movimentazione = new Movimentazione();
        //movimentazione.setIdGiacenza(giacenzaArticolo.getId());
        movimentazione.setInputOutput(OUTPUT);
        if(fatturaAccompagnatoriaArticolo.getFatturaAccompagnatoria() != null){
            movimentazione.setData(fatturaAccompagnatoriaArticolo.getFatturaAccompagnatoria().getData());
        }
        movimentazione.setPezzi(fatturaAccompagnatoriaArticolo.getNumeroPezzi());
        movimentazione.setQuantita(fatturaAccompagnatoriaArticolo.getQuantita());
        movimentazione.setDescrizione(createDescrizione(fatturaAccompagnatoriaArticolo, lotto, scadenza, articolo));
        movimentazione.setDataInserimento(fatturaAccompagnatoriaArticolo.getDataInserimento());

        return movimentazione;
    }

    private Movimentazione create(RicevutaPrivatoArticolo ricevutaPrivatoArticolo, String lotto, Date scadenza, Articolo articolo){
        Movimentazione movimentazione = new Movimentazione();
        //movimentazione.setIdGiacenza(giacenzaArticolo.getId());
        movimentazione.setInputOutput(OUTPUT);
        if(ricevutaPrivatoArticolo.getRicevutaPrivato() != null){
            movimentazione.setData(ricevutaPrivatoArticolo.getRicevutaPrivato().getData());
        }
        movimentazione.setPezzi(ricevutaPrivatoArticolo.getNumeroPezzi());
        movimentazione.setQuantita(ricevutaPrivatoArticolo.getQuantita());
        movimentazione.setDescrizione(createDescrizione(ricevutaPrivatoArticolo, lotto, scadenza, articolo));
        movimentazione.setDataInserimento(ricevutaPrivatoArticolo.getDataInserimento());

        return movimentazione;
    }

    private Movimentazione create(DdtAcquistoArticolo ddtAcquistoArticolo, String lotto, Date scadenza, Articolo articolo){
        Movimentazione movimentazione = new Movimentazione();
        //movimentazione.setIdGiacenza(giacenzaArticolo.getId());
        movimentazione.setInputOutput(INPUT);
        if(ddtAcquistoArticolo.getDdtAcquisto() != null){
            movimentazione.setData(ddtAcquistoArticolo.getDdtAcquisto().getData());
        }
        movimentazione.setPezzi(ddtAcquistoArticolo.getNumeroPezzi());
        movimentazione.setQuantita(ddtAcquistoArticolo.getQuantita());
        movimentazione.setDescrizione(createDescrizione(ddtAcquistoArticolo, lotto, scadenza, articolo));
        movimentazione.setDataInserimento(ddtAcquistoArticolo.getDataInserimento());

        return movimentazione;
    }

    private Movimentazione create(ProduzioneConfezione produzioneConfezione, String lotto, Date scadenza){
        Produzione produzione = produzioneRepository.findById(produzioneConfezione.getProduzione().getId()).get();

        Movimentazione movimentazione = new Movimentazione();
        //movimentazione.setIdGiacenza(giacenzaArticolo.getId());
        movimentazione.setInputOutput(INPUT);
        movimentazione.setData(produzione.getDataProduzione());
        Integer pezzi = produzioneConfezione.getNumConfezioniProdotte() != null ? produzioneConfezione.getNumConfezioniProdotte() : 0;
        Float quantita = 0f;
        Confezione confezione = produzioneConfezione.getConfezione();
        if(confezione != null){
            quantita = pezzi * (confezione.getPeso()/1000);
        }
        movimentazione.setPezzi(pezzi);
        movimentazione.setQuantita(quantita);
        movimentazione.setDescrizione(createDescrizione(produzione, lotto, scadenza, pezzi, quantita, produzioneConfezione.getArticolo()));
        movimentazione.setDataInserimento(produzione.getDataInserimento());

        return movimentazione;
    }

    private Movimentazione create(FatturaAccompagnatoriaAcquistoArticolo fatturaAccompagnatoriaAcquistoArticolo, String lotto, Date scadenza, Articolo articolo){
        Movimentazione movimentazione = new Movimentazione();
        //movimentazione.setIdGiacenza(giacenzaArticolo.getId());
        movimentazione.setInputOutput(INPUT);
        if(fatturaAccompagnatoriaAcquistoArticolo.getFatturaAccompagnatoriaAcquisto() != null){
            movimentazione.setData(fatturaAccompagnatoriaAcquistoArticolo.getFatturaAccompagnatoriaAcquisto().getData());
        }
        movimentazione.setPezzi(fatturaAccompagnatoriaAcquistoArticolo.getNumeroPezzi());
        movimentazione.setQuantita(fatturaAccompagnatoriaAcquistoArticolo.getQuantita());
        movimentazione.setDescrizione(createDescrizione(fatturaAccompagnatoriaAcquistoArticolo, lotto, scadenza, articolo));
        movimentazione.setDataInserimento(fatturaAccompagnatoriaAcquistoArticolo.getDataInserimento());

        return movimentazione;
    }

    private Movimentazione create(MovimentazioneManualeArticolo movimentazioneManualeArticolo, String lotto, Articolo articolo){
        Movimentazione movimentazione = new Movimentazione();
        //movimentazione.setIdGiacenza(giacenzaArticolo.getId());

        Operation operation = StringUtils.isNotEmpty(movimentazioneManualeArticolo.getOperation()) ? Operation.valueOf(movimentazioneManualeArticolo.getOperation()) : Operation.CREATE;

        if(Operation.CREATE.equals(operation)){
            movimentazione.setInputOutput(INPUT);
        } else if(Operation.DELETE.equals(operation)){
            if(StringUtils.isNotEmpty(movimentazioneManualeArticolo.getContext())){
                Resource resource = Resource.valueOf(movimentazioneManualeArticolo.getContext());
                if(Resource.DDT.equals(resource) || Resource.FATTURA_ACCOMPAGNATORIA.equals(resource) || Resource.RICEVUTA_PRIVATO.equals(resource)){
                    movimentazione.setInputOutput(INPUT);
                } else if(Resource.DDT_ACQUISTO.equals(resource) || Resource.PRODUZIONE.equals(resource) || Resource.FATTURA_ACCOMPAGNATORIA_ACQUISTO.equals(resource)){
                    movimentazione.setInputOutput(OUTPUT);
                }
            }
        }
        movimentazione.setData(new Date(movimentazioneManualeArticolo.getDataInserimento().getTime()));
        movimentazione.setQuantita(movimentazioneManualeArticolo.getQuantita());
        movimentazione.setPezzi(movimentazioneManualeArticolo.getPezzi());
        movimentazione.setDescrizione(createDescrizione(movimentazioneManualeArticolo, lotto, articolo));
        movimentazione.setDataInserimento(movimentazioneManualeArticolo.getDataInserimento());

        return movimentazione;
    }

    private String createDescrizioneQuantita(Articolo articolo, String acquistoVendita, Integer pezzi, Float quantita){
        UnitaMisura unitaMisura = articolo.getUnitaMisura();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(acquistoVendita);
        if(unitaMisura != null){
            if(unitaMisura.getNome().equals("pz")){
                stringBuilder.append(pezzi).append("</b>");
            } else {
                stringBuilder.append(quantita).append("</b>");
            }
            stringBuilder.append(" ").append(unitaMisura.getEtichetta());
        } else {
            stringBuilder.append(pezzi).append("</b>");
        }
        return stringBuilder.toString();
    }

    private String createDescrizione(DdtArticolo ddtArticolo, String lotto, Date scadenza, Articolo articolo){
        Ddt ddt = ddtRepository.findById(ddtArticolo.getId().getDdtId()).get();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(createDescrizioneQuantita(articolo, "Venduto/i <b>", ddtArticolo.getNumeroPezzi(), ddtArticolo.getQuantita()));
        stringBuilder.append(" il ").append(simpleDateFormat.format(ddt.getData()));
        stringBuilder.append(" lotto <b>").append(lotto).append("</b>");
        stringBuilder.append(" scadenza <b>");
        if(scadenza != null){
            stringBuilder.append(simpleDateFormat.format(scadenza));
        } else {
            stringBuilder.append("ND");
        }
        stringBuilder.append("</b>");
        stringBuilder.append(" (DDT n. ").append(ddt.getProgressivo()).append("/").append(ddt.getAnnoContabile()).append(")");

        return stringBuilder.toString();
    }

    private String createDescrizione(FatturaAccompagnatoriaArticolo fatturaAccompagnatoriaArticolo, String lotto, Date scadenza, Articolo articolo){
        FatturaAccompagnatoria fatturaAccompagnatoria = fatturaAccompagnatoriaRepository.findById(fatturaAccompagnatoriaArticolo.getId().getFatturaAccompagnatoriaId()).get();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(createDescrizioneQuantita(articolo, "Venduto/i <b>", fatturaAccompagnatoriaArticolo.getNumeroPezzi(), fatturaAccompagnatoriaArticolo.getQuantita()));
        stringBuilder.append(" il ").append(simpleDateFormat.format(fatturaAccompagnatoria.getData()));
        stringBuilder.append(" lotto <b>").append(lotto).append("</b>");
        stringBuilder.append(" scadenza <b>");
        if(scadenza != null){
            stringBuilder.append(simpleDateFormat.format(scadenza));
        } else {
            stringBuilder.append("ND");
        }
        stringBuilder.append("</b>");
        stringBuilder.append(" (Fattura accompagnatoria n. ").append(fatturaAccompagnatoria.getProgressivo()).append("/").append(fatturaAccompagnatoria.getAnno()).append(")");

        return stringBuilder.toString();
    }

    private String createDescrizione(RicevutaPrivatoArticolo ricevutaPrivatoArticolo, String lotto, Date scadenza, Articolo articolo){
        RicevutaPrivato ricevutaPrivato = ricevutaPrivatoRepository.findById(ricevutaPrivatoArticolo.getId().getRicevutaPrivatoId()).get();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(createDescrizioneQuantita(articolo, "Venduto/i <b>", ricevutaPrivatoArticolo.getNumeroPezzi(), ricevutaPrivatoArticolo.getQuantita()));
        stringBuilder.append(" il ").append(simpleDateFormat.format(ricevutaPrivato.getData()));
        stringBuilder.append(" lotto <b>").append(lotto).append("</b>");
        stringBuilder.append(" scadenza <b>");
        if(scadenza != null){
            stringBuilder.append(simpleDateFormat.format(scadenza));
        } else {
            stringBuilder.append("ND");
        }
        stringBuilder.append("</b>");
        stringBuilder.append(" (Ricevuta privato n. ").append(ricevutaPrivato.getProgressivo()).append("/").append(ricevutaPrivato.getAnno()).append(")");

        return stringBuilder.toString();
    }

    private String createDescrizione(DdtAcquistoArticolo ddtAcquistoArticolo, String lotto, Date scadenza, Articolo articolo){
        Fornitore fornitore = articolo.getFornitore();
        DdtAcquisto ddtAcquisto = ddtAcquistoRepository.findById(ddtAcquistoArticolo.getId().getDdtAcquistoId()).get();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(createDescrizioneQuantita(articolo, "Acquistato/i <b>", ddtAcquistoArticolo.getNumeroPezzi(), ddtAcquistoArticolo.getQuantita()));
        stringBuilder.append(" il ").append(simpleDateFormat.format(ddtAcquisto.getData()));
        stringBuilder.append(" lotto <b>").append(lotto).append("</b>");
        stringBuilder.append(" scadenza <b>");
        if(scadenza != null){
            stringBuilder.append(simpleDateFormat.format(scadenza));
        } else {
            stringBuilder.append("ND");
        }
        stringBuilder.append("</b>");
        stringBuilder.append(" (DDT acquisto n. ").append(ddtAcquisto.getNumero());
        if(fornitore != null){
            stringBuilder.append(" da ");
            stringBuilder.append(fornitore.getRagioneSociale());
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    private String createDescrizione(Produzione produzione, String lotto, Date scadenza, Integer pezzi, Float quantita, Articolo articolo){
        Fornitore fornitore = fornitoreService.getByRagioneSociale(Constants.DEFAULT_FORNITORE);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(createDescrizioneQuantita(articolo, "Prodotto/i <b>", pezzi, quantita));
        stringBuilder.append(" il ").append(simpleDateFormat.format(produzione.getDataProduzione()));
        stringBuilder.append(" lotto <b>").append(lotto).append("</b>");
        stringBuilder.append(" scadenza <b>");
        if(scadenza != null){
            stringBuilder.append(simpleDateFormat.format(scadenza));
        } else {
            stringBuilder.append("ND");
        }
        stringBuilder.append("</b>");
        stringBuilder.append(" (Produzione");
        if(produzione.getTipologia().equals("SCORTA")){
            stringBuilder.append(" scorta");
        }
        stringBuilder.append(" n. ").append(produzione.getCodice());
        if(fornitore != null){
            stringBuilder.append(" da ");
            stringBuilder.append(fornitore.getRagioneSociale());
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    private String createDescrizione(Produzione produzione, String lotto, Date scadenza, Float quantita){
        Fornitore fornitore = fornitoreService.getByRagioneSociale(Constants.DEFAULT_FORNITORE);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Prodotto/i <b>").append(quantita).append("</b>");
        stringBuilder.append(" il ").append(simpleDateFormat.format(produzione.getDataProduzione()));
        stringBuilder.append(" lotto <b>").append(lotto).append("</b>");
        stringBuilder.append(" scadenza <b>");
        if(scadenza != null){
            stringBuilder.append(simpleDateFormat.format(scadenza));
        } else {
            stringBuilder.append("ND");
        }
        stringBuilder.append("</b>");
        stringBuilder.append(" (Produzione");
        if(produzione.getTipologia().equals("SCORTA")){
            stringBuilder.append(" scorta");
        }
        stringBuilder.append(" n. ").append(produzione.getCodice());
        if(fornitore != null){
            stringBuilder.append(" da ");
            stringBuilder.append(fornitore.getRagioneSociale());
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    private String createDescrizione(FatturaAccompagnatoriaAcquistoArticolo fatturaAccompagnatoriaAcquistoArticolo, String lotto, Date scadenza, Articolo articolo){
        Fornitore fornitore = articolo.getFornitore();
        FatturaAccompagnatoriaAcquisto fatturaAccompagnatoriaAcquisto = fatturaAccompagnatoriaAcquistoRepository.findById(fatturaAccompagnatoriaAcquistoArticolo.getId().getFatturaAccompagnatoriaAcquistoId()).get();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(createDescrizioneQuantita(articolo, "Acquistato/i <b>", fatturaAccompagnatoriaAcquistoArticolo.getNumeroPezzi(), fatturaAccompagnatoriaAcquistoArticolo.getQuantita()));
        stringBuilder.append(" il ").append(simpleDateFormat.format(fatturaAccompagnatoriaAcquisto.getData()));
        stringBuilder.append(" lotto <b>").append(lotto).append("</b>");
        stringBuilder.append(" scadenza <b>");
        if(scadenza != null){
            stringBuilder.append(simpleDateFormat.format(scadenza));
        } else {
            stringBuilder.append("ND");
        }
        stringBuilder.append("</b>");
        stringBuilder.append(" (Fattura accompagnatoria acquisto n. ").append(fatturaAccompagnatoriaAcquisto.getNumero());
        if(fornitore != null){
            stringBuilder.append(" da ");
            stringBuilder.append(fornitore.getRagioneSociale());
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    private String createDescrizione(MovimentazioneManualeArticolo movimentazioneManualeArticolo, String lotto, Articolo articolo){

        String acquistoVendita = "Inserito/i <b>";
        String descrizione = " (Inserimento manuale)";

        Operation operation = StringUtils.isNotEmpty(movimentazioneManualeArticolo.getOperation()) ? Operation.valueOf(movimentazioneManualeArticolo.getOperation()) : Operation.CREATE;

        if(Operation.DELETE.equals(operation)){
            if(StringUtils.isNotEmpty(movimentazioneManualeArticolo.getContext())){
                acquistoVendita = "Reinserito/i <b>";

                Resource resource = Resource.valueOf(movimentazioneManualeArticolo.getContext());
                if(Resource.DDT.equals(resource)){
                    descrizione = " (Rimozione DDT n. "+movimentazioneManualeArticolo.getNumDocumento()+"/"+movimentazioneManualeArticolo.getAnnoDocumento()+")";
                } else if(Resource.FATTURA_ACCOMPAGNATORIA.equals(resource)){
                    descrizione = " (Rimozione Fattura accompagnatoria n. "+movimentazioneManualeArticolo.getNumDocumento()+"/"+movimentazioneManualeArticolo.getAnnoDocumento()+")";
                } else if(Resource.RICEVUTA_PRIVATO.equals(resource)){
                    descrizione = " (Rimozione Ricevuta privato n. "+movimentazioneManualeArticolo.getNumDocumento()+"/"+movimentazioneManualeArticolo.getAnnoDocumento()+")";
                } else if(Resource.DDT_ACQUISTO.equals(resource)){
                    descrizione = " (Rimozione DDT acquisto n. "+movimentazioneManualeArticolo.getNumDocumento()+" da "+movimentazioneManualeArticolo.getFornitoreDocumento()+")";
                } else if(Resource.PRODUZIONE.equals(resource)) {
                    descrizione = " (Rimozione Produzione n. "+movimentazioneManualeArticolo.getNumDocumento()+" da "+movimentazioneManualeArticolo.getFornitoreDocumento()+")";
                } else if(Resource.FATTURA_ACCOMPAGNATORIA_ACQUISTO.equals(resource)){
                    descrizione = " (Rimozione Fattura accompagnatoria acquisto n. "+movimentazioneManualeArticolo.getNumDocumento()+" da "+movimentazioneManualeArticolo.getFornitoreDocumento()+")";
                }
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(createDescrizioneQuantita(articolo, acquistoVendita, movimentazioneManualeArticolo.getPezzi(), movimentazioneManualeArticolo.getQuantita()));
        stringBuilder.append(" il ").append(simpleDateFormat.format(new Date(movimentazioneManualeArticolo.getDataInserimento().getTime())));
        stringBuilder.append(" lotto <b>").append(lotto).append("</b>");
        stringBuilder.append(" scadenza <b>");
        if(movimentazioneManualeArticolo.getScadenza() != null){
            stringBuilder.append(simpleDateFormat.format(movimentazioneManualeArticolo.getScadenza()));
        } else {
            stringBuilder.append("ND");
        }
        stringBuilder.append("</b>");
        stringBuilder.append(descrizione);

        return stringBuilder.toString();
    }

    private String createDescrizione(DdtAcquistoIngrediente ddtAcquistoIngrediente, GiacenzaIngrediente giacenzaIngrediente, Ingrediente ingrediente){
        //UnitaMisura unitaMisura = ingrediente.getUnitaMisura();
        Fornitore fornitore = ingrediente.getFornitore();
        DdtAcquisto ddtAcquisto = ddtAcquistoRepository.findById(ddtAcquistoIngrediente.getId().getDdtAcquistoId()).get();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Acquistato/i <b>").append(ddtAcquistoIngrediente.getQuantita()).append("</b>");
        /*if(unitaMisura != null){
            stringBuilder.append(" ").append(unitaMisura.getEtichetta());
        } else {
            stringBuilder.append("ND");
        }*/
        stringBuilder.append(" il ").append(simpleDateFormat.format(ddtAcquisto.getData()));
        stringBuilder.append(" lotto <b>").append(giacenzaIngrediente.getLotto()).append("</b>");
        stringBuilder.append(" scadenza <b>");
        if(giacenzaIngrediente.getScadenza() != null){
            stringBuilder.append(simpleDateFormat.format(giacenzaIngrediente.getScadenza()));
        } else {
            stringBuilder.append("ND");
        }
        stringBuilder.append("</b> (DDT acquisto n. ").append(ddtAcquisto.getNumero());
        if(fornitore != null){
            stringBuilder.append(" da ");
            stringBuilder.append(fornitore.getRagioneSociale());
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    private String createDescrizione(MovimentazioneManualeIngrediente movimentazioneManualeIngrediente, GiacenzaIngrediente giacenzaIngrediente){
        //UnitaMisura unitaMisura = ingrediente.getUnitaMisura();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Inserito/i <b>").append(movimentazioneManualeIngrediente.getQuantita()).append("</b>");
        /*if(unitaMisura != null){
            stringBuilder.append(" ").append(unitaMisura.getEtichetta());
        }*/
        stringBuilder.append(" il ").append(simpleDateFormat.format(new Date(movimentazioneManualeIngrediente.getDataInserimento().getTime())));
        stringBuilder.append(" lotto <b>").append(giacenzaIngrediente.getLotto()).append("</b>");
        stringBuilder.append(" scadenza <b>");
        if(movimentazioneManualeIngrediente.getScadenza() != null){
            stringBuilder.append(simpleDateFormat.format(movimentazioneManualeIngrediente.getScadenza()));
        } else {
            stringBuilder.append("ND");
        }
        stringBuilder.append("</b>");
        stringBuilder.append(" (Inserimento manuale)");

        return stringBuilder.toString();
    }

}
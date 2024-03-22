package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.*;
import com.contarbn.model.beans.SchedaTecnicaResponse;
import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VSchedaTecnica;
import com.contarbn.model.views.VSchedaTecnicaLight;
import com.contarbn.repository.*;
import com.contarbn.repository.views.VSchedaTecnicaLightRepository;
import com.contarbn.repository.views.VSchedaTecnicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class SchedaTecnicaService {

    private final SchedaTecnicaRaccoltaRepository schedaTecnicaRaccoltaRepository;
    private final SchedaTecnicaAnalisiRepository schedaTecnicaAnalisiRepository;
    private final SchedaTecnicaNutrienteRepository schedaTecnicaNutrienteRepository;
    private final SchedaTecnicaRepository schedaTecnicaRepository;
    private final VSchedaTecnicaRepository vSchedaTecnicaRepository;
    private final VSchedaTecnicaLightRepository vSchedaTecnicaLightRepository;
    private final RicettaNutrienteRepository ricettaNutrienteRepository;
    private final RicettaAnalisiRepository ricettaAnalisiRepository;
    private final TransactionTemplate transactionTemplate;

    public List<VSchedaTecnicaLight> getAllByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, String prodotto){
        log.info("Retrieving the list of 'schede-tecniche' filtered by request parameters");
        List<VSchedaTecnicaLight> produzioni = vSchedaTecnicaLightRepository.findByFilters(draw, start, length, sortOrders, prodotto);
        log.info("Retrieved {} 'schede-tecniche'", produzioni.size());
        return produzioni;
    }

    public Integer getCountByFilters(String prodotto){
        log.info("Retrieving the count of 'schede-tecniche' filtered by request parameters");
        Integer count = vSchedaTecnicaLightRepository.countByFilters(prodotto);
        log.info("Retrieved {} 'schede-tecniche'", count);
        return count;
    }

    public SchedaTecnicaResponse get(Long idProduzione, Long idArticolo){
        log.info("Retrieving 'scheda tecnica' for 'produzione' {} and 'articolo' {}", idProduzione, idArticolo);
        Optional<SchedaTecnica> schedaTecnica = getByIdProduzioneAndIdArticolo(idProduzione, idArticolo);
        if(!schedaTecnica.isPresent()) {
            return createResponse(getByIdProduzioneAndIdArticoloFromView(idProduzione, idArticolo));
        }
        return createResponse(schedaTecnica.get());
    }

    public SchedaTecnica getById(Long idSchedaTecnica){
        log.info("Retrieving 'scheda tecnica' with id '{}'", idSchedaTecnica);
        return schedaTecnicaRepository.findById(idSchedaTecnica).orElseThrow(RuntimeException::new);
    }

    public VSchedaTecnica getByIdProduzioneAndIdArticoloFromView(Long idProduzione, Long idArticolo){
        log.info("Retrieving 'scheda tecnica' view for 'produzione' {} and 'articolo' {}", idProduzione, idArticolo);
        VSchedaTecnica vSchedaTecnica = vSchedaTecnicaRepository.findFirstByIdProduzioneAndIdArticolo(idProduzione, idArticolo).orElseThrow(ResourceNotFoundException::new);
        if(vSchedaTecnica.getNumRevisione() == null){
            vSchedaTecnica.setNumRevisione(getNumRevisione(vSchedaTecnica.getAnno()));
        }
        log.info("Retrieved 'scheda tecnica' view  '{}'", vSchedaTecnica);
        return vSchedaTecnica;
    }

    public Optional<SchedaTecnica> getByIdProduzioneAndIdArticolo(Long idProduzione, Long idArticolo){
        log.info("Retrieving 'scheda tecnica' for 'produzione' {} and 'articolo' {}", idProduzione, idArticolo);
        Optional<SchedaTecnica> optionalSchedaTecnica = schedaTecnicaRepository.findFirstByIdProduzioneAndIdArticolo(idProduzione, idArticolo);
        if(!optionalSchedaTecnica.isPresent()){
            return optionalSchedaTecnica;
        }
        SchedaTecnica schedaTecnica = optionalSchedaTecnica.get();
        if(schedaTecnica.getAnno() == null){
            Date data = schedaTecnica.getData() != null ? schedaTecnica.getData() : Date.valueOf(ZonedDateTime.now().toLocalDate());
            int anno = data.toLocalDate().getYear();
            int numRevisione = getNumRevisione(anno);
            if(schedaTecnica.getData() == null){
                schedaTecnica.setData(Date.valueOf(ZonedDateTime.now().toLocalDate()));
            }
            schedaTecnica.setAnno(anno);
            schedaTecnica.setNumRevisione(numRevisione);
        }
        return Optional.of(schedaTecnica);
    }

    public SchedaTecnica save(SchedaTecnica schedaTecnica){
        log.info("Saving 'scheda tecnica'");

        Timestamp dataInserimento = Timestamp.from(ZonedDateTime.now().toInstant());
        Optional<SchedaTecnica> currentSchedaTecnica = Optional.empty();
        if(schedaTecnica.getId() != null){
            currentSchedaTecnica = schedaTecnicaRepository.findById(schedaTecnica.getId());
        }
        if(currentSchedaTecnica.isPresent()){
            dataInserimento = currentSchedaTecnica.get().getDataInserimento();
        }
        schedaTecnica.setDataInserimento(dataInserimento);
        schedaTecnica.setNumRevisione(schedaTecnica.getNumRevisione() != null ? schedaTecnica.getNumRevisione() : 1);
        schedaTecnica.setData(schedaTecnica.getData() != null ? schedaTecnica.getData() : Date.valueOf(ZonedDateTime.now().toLocalDate()));
        schedaTecnica.setAnno(schedaTecnica.getAnno() != null ? schedaTecnica.getAnno() : ZonedDateTime.now().toLocalDate().getYear());
        schedaTecnica.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));

        SchedaTecnica resultSchedaTecnica;

        resultSchedaTecnica = transactionTemplate.execute(status -> {
            Set<SchedaTecnicaRaccolta> schedaTecnicaRaccolte = schedaTecnica.getSchedaTecnicaRaccolte();
            Set<SchedaTecnicaAnalisi> schedaTecnicaAnalisi = schedaTecnica.getSchedaTecnicaAnalisi();
            Set<SchedaTecnicaNutriente> schedaTecnicaDichiarazioni = schedaTecnica.getSchedaTecnicaNutrienti();

            if(schedaTecnica.getId() != null){
                schedaTecnica.setSchedaTecnicaRaccolte(new HashSet<>());
                schedaTecnicaRaccoltaRepository.deleteBySchedaTecnicaId(schedaTecnica.getId());

                schedaTecnica.setSchedaTecnicaAnalisi(new HashSet<>());
                schedaTecnicaAnalisiRepository.deleteBySchedaTecnicaId(schedaTecnica.getId());

                schedaTecnica.setSchedaTecnicaNutrienti(new HashSet<>());
                schedaTecnicaNutrienteRepository.deleteBySchedaTecnicaId(schedaTecnica.getId());
            }

            SchedaTecnica updatedSchedaTecnica = schedaTecnicaRepository.save(schedaTecnica);
            schedaTecnicaRaccolte.forEach(r -> {
                r.getId().setSchedaTecnicaId(updatedSchedaTecnica.getId());
                r.setSchedaTecnica(updatedSchedaTecnica);
                schedaTecnicaRaccoltaRepository.save(r);
            });
            schedaTecnicaAnalisi.forEach(a -> {
                a.getId().setSchedaTecnicaId(updatedSchedaTecnica.getId());
                a.setSchedaTecnica(updatedSchedaTecnica);
                schedaTecnicaAnalisiRepository.save(a);
            });
            schedaTecnicaDichiarazioni.forEach(d -> {
                d.getId().setSchedaTecnicaId(updatedSchedaTecnica.getId());
                d.setSchedaTecnica(updatedSchedaTecnica);
                schedaTecnicaNutrienteRepository.save(d);
            });

            return updatedSchedaTecnica;
        });

        log.info("Saved 'scheda tecnica' '{}'", resultSchedaTecnica);
        return resultSchedaTecnica;
    }

    public void patch(Map<String,Object> patchSchedaTecnica){
        log.info("Patching 'scheda tecnica'");

        Long id = (Long) patchSchedaTecnica.get("id");
        SchedaTecnica schedaTecnica = getById(id);
        patchSchedaTecnica.forEach((key, value) -> {
            switch (key) {
                case "id":
                    schedaTecnica.setId((Long)value);
                    break;
                case "pdf":
                    schedaTecnica.setPdf((byte[])value);
                    break;
            }
        });
        SchedaTecnica resultSchedaTecnica;

        resultSchedaTecnica = transactionTemplate.execute(status -> schedaTecnicaRepository.save(schedaTecnica));

        log.info("Patched 'scheda tecnica' '{}'", resultSchedaTecnica);
    }

    public void delete(Long idSchedaTecnica){
        log.info("Deleting 'scheda tecnica' {}", idSchedaTecnica);

        transactionTemplate.execute(status -> {
            schedaTecnicaRaccoltaRepository.deleteBySchedaTecnicaId(idSchedaTecnica);
            schedaTecnicaAnalisiRepository.deleteBySchedaTecnicaId(idSchedaTecnica);
            schedaTecnicaNutrienteRepository.deleteBySchedaTecnicaId(idSchedaTecnica);
            schedaTecnicaRepository.deleteById(idSchedaTecnica);
            return null;
        });

        log.info("Deleted 'scheda tecnica' {}", idSchedaTecnica);
    }

    public Map<String, Integer> getNumRevisioneAndAnno(Date data){
        Integer anno = data != null ? data.toLocalDate().getYear() : ZonedDateTime.now().toLocalDate().getYear();
        Integer numRevisione = getNumRevisione(anno);
        HashMap<String, Integer> result = new HashMap<>();
        result.put("anno", anno);
        result.put("numRevisione", numRevisione);

        return result;
    }

    private Integer getNumRevisione(Integer anno){
        int numRevisione = 1;
        Integer resultNumRevisione = schedaTecnicaRepository.getLastNumRevisioneByAnno(anno);
        if(resultNumRevisione != null){
            numRevisione = resultNumRevisione + 1;
        }
        return numRevisione;
    }

    private SchedaTecnicaResponse createResponse(VSchedaTecnica vSchedaTecnica){

        SchedaTecnicaResponse schedaTecnicaResponse = SchedaTecnicaResponse.builder()
                .idView(vSchedaTecnica.getId())
                .id(vSchedaTecnica.getIdSchedaTecnica())
                .idProduzione(vSchedaTecnica.getIdProduzione())
                .idArticolo(vSchedaTecnica.getIdArticolo())
                .idRicetta(vSchedaTecnica.getIdRicetta())
                .numRevisione(vSchedaTecnica.getNumRevisione())
                .anno(vSchedaTecnica.getAnno())
                .data(vSchedaTecnica.getData())
                .codiceProdotto(vSchedaTecnica.getCodiceProdotto())
                .prodotto(vSchedaTecnica.getProdotto())
                .prodotto2(vSchedaTecnica.getProdotto2())
                .pesoNettoConfezione(vSchedaTecnica.getPesoNettoConfezione())
                .ingredienti(vSchedaTecnica.getIngredienti())
                .tracce(vSchedaTecnica.getTracce())
                .durata(vSchedaTecnica.getDurata())
                .conservazione(vSchedaTecnica.getConservazione())
                .consigliConsumo(vSchedaTecnica.getConsigliConsumo())
                .imballoDimensioni(vSchedaTecnica.getImballoDimensioni())
                .objectType("view")
                .build();

        if(vSchedaTecnica.getIdTipologiaConfezionamento() != null){
            Anagrafica tipologiaConfezionamento = new Anagrafica();
            tipologiaConfezionamento.setId(vSchedaTecnica.getIdTipologiaConfezionamento());
            tipologiaConfezionamento.setNome(vSchedaTecnica.getTipologiaConfezionamento());

            schedaTecnicaResponse.setTipologiaConfezionamento(tipologiaConfezionamento);
        }

        if(vSchedaTecnica.getIdImballo() != null){
            Anagrafica imballo = new Anagrafica();
            imballo.setId(vSchedaTecnica.getIdImballo());
            imballo.setNome(vSchedaTecnica.getImballo());

            schedaTecnicaResponse.setImballo(imballo);
        }

        Set<SchedaTecnicaNutriente> schedaTecnicaNutrienti = new HashSet<>();
        Set<SchedaTecnicaAnalisi> schedaTecnicaAnalisi = new HashSet<>();
        if(vSchedaTecnica.getIdRicetta() != null){
            Set<RicettaNutriente> ricettaNutrienti = ricettaNutrienteRepository.findByRicettaId(vSchedaTecnica.getIdRicetta());
            if(!ricettaNutrienti.isEmpty()){
                for(RicettaNutriente ricettaNutriente : ricettaNutrienti){
                    SchedaTecnicaNutrienteKey schedaTecnicaNutrienteKey = new SchedaTecnicaNutrienteKey();
                    schedaTecnicaNutrienteKey.setNutrienteId(ricettaNutriente.getId().getNutrienteId());

                    SchedaTecnicaNutriente schedaTecnicaNutriente = new SchedaTecnicaNutriente();
                    schedaTecnicaNutriente.setId(schedaTecnicaNutrienteKey);
                    schedaTecnicaNutriente.setNutriente(ricettaNutriente.getNutriente());
                    schedaTecnicaNutriente.setValore(ricettaNutriente.getValore());

                    schedaTecnicaNutrienti.add(schedaTecnicaNutriente);
                }
            }

            Set<RicettaAnalisi> ricettaAnalisiList = ricettaAnalisiRepository.findByRicettaId(vSchedaTecnica.getIdRicetta());
            if(!ricettaAnalisiList.isEmpty()){
                for(RicettaAnalisi ricettaAnalisi : ricettaAnalisiList){
                    SchedaTecnicaAnalisiKey schedaTecnicaAnalisiKey = new SchedaTecnicaAnalisiKey();
                    schedaTecnicaAnalisiKey.setAnalisiId(ricettaAnalisi.getId().getAnalisiId());

                    SchedaTecnicaAnalisi schedaTecnicaAnalisi2 = new SchedaTecnicaAnalisi();
                    schedaTecnicaAnalisi2.setId(schedaTecnicaAnalisiKey);
                    schedaTecnicaAnalisi2.setAnalisi(ricettaAnalisi.getAnalisi());
                    schedaTecnicaAnalisi2.setRisultato(ricettaAnalisi.getRisultato());

                    schedaTecnicaAnalisi.add(schedaTecnicaAnalisi2);
                }
            }
        }

        schedaTecnicaResponse.setSchedaTecnicaNutrienti(schedaTecnicaNutrienti);
        schedaTecnicaResponse.setSchedaTecnicaAnalisi(schedaTecnicaAnalisi);
        return schedaTecnicaResponse;
    }

    private SchedaTecnicaResponse createResponse(SchedaTecnica schedaTecnica){

        return SchedaTecnicaResponse.builder()
                .id(schedaTecnica.getId())
                .idProduzione(schedaTecnica.getIdProduzione())
                .idArticolo(schedaTecnica.getIdArticolo())
                .numRevisione(schedaTecnica.getNumRevisione())
                .anno(schedaTecnica.getAnno())
                .data(schedaTecnica.getData())
                .codiceProdotto(schedaTecnica.getCodiceProdotto())
                .prodotto(schedaTecnica.getProdotto())
                .prodotto2(schedaTecnica.getProdotto2())
                .pesoNettoConfezione(schedaTecnica.getPesoNettoConfezione())
                .ingredienti(schedaTecnica.getIngredienti())
                .tracce(schedaTecnica.getTracce())
                .durata(schedaTecnica.getDurata())
                .conservazione(schedaTecnica.getConservazione())
                .consigliConsumo(schedaTecnica.getConsigliConsumo())
                .tipologiaConfezionamento(schedaTecnica.getTipologiaConfezionamento())
                .imballo(schedaTecnica.getImballo())
                .imballoDimensioni(schedaTecnica.getImballoDimensioni())
                .dataInserimento(schedaTecnica.getDataInserimento())
                .dataAggiornamento(schedaTecnica.getDataAggiornamento())
                .objectType("table")
                .schedaTecnicaNutrienti(schedaTecnica.getSchedaTecnicaNutrienti())
                .schedaTecnicaAnalisi(schedaTecnica.getSchedaTecnicaAnalisi())
                .schedaTecnicaRaccolte(schedaTecnica.getSchedaTecnicaRaccolte())
                .build();

    }

}
package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.SchedaTecnica;
import com.contarbn.model.SchedaTecnicaAnalisi;
import com.contarbn.model.SchedaTecnicaNutriente;
import com.contarbn.model.SchedaTecnicaRaccolta;
import com.contarbn.model.views.VSchedaTecnica;
import com.contarbn.repository.SchedaTecnicaAnalisiRepository;
import com.contarbn.repository.SchedaTecnicaNutrienteRepository;
import com.contarbn.repository.SchedaTecnicaRaccoltaRepository;
import com.contarbn.repository.SchedaTecnicaRepository;
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
    private final TransactionTemplate transactionTemplate;

    public Object getSchedaTecnica(Long idProduzione, Long idArticolo){
        log.info("Retrieving 'scheda tecnica' for 'produzione' {} and 'articolo' {}", idProduzione, idArticolo);
        Optional<SchedaTecnica> schedaTecnica = getByIdProduzioneAndIdArticolo(idProduzione, idArticolo);
        if(!schedaTecnica.isPresent()) {
            return getByIdProduzioneAndIdArticoloFromView(idProduzione, idArticolo);
        }
        return schedaTecnica;
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

}
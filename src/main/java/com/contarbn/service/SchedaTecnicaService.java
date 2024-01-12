package com.contarbn.service;

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
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

    public SchedaTecnica getById(Long idSchedaTecnica){
        log.info("Retrieving 'scheda tecnica' with id '{}'", idSchedaTecnica);
        return schedaTecnicaRepository.findById(idSchedaTecnica).orElseThrow(RuntimeException::new);
    }

    public VSchedaTecnica getByIdRicettaFromView(Long idRicetta){
        log.info("Retrieving 'scheda tecnica' view for ricetta '{}'", idRicetta);
        VSchedaTecnica vSchedaTecnica = vSchedaTecnicaRepository.findFirstByIdRicetta(idRicetta).orElseThrow(RuntimeException::new);
        log.info("Retrieved 'scheda tecnica' view  '{}'", vSchedaTecnica);
        return vSchedaTecnica;
    }

    public Optional<SchedaTecnica> getByIdRicetta(Long idRicetta){
        log.info("Retrieving 'scheda tecnica' for ricetta '{}'", idRicetta);
        return schedaTecnicaRepository.findFirstByIdRicetta(idRicetta);
    }

    public SchedaTecnica save(SchedaTecnica schedaTecnica){
        log.info("Saving 'scheda tecnica'");

        schedaTecnica.setNumRevisione(schedaTecnica.getNumRevisione() != null ? schedaTecnica.getNumRevisione() : 1);
        schedaTecnica.setData(schedaTecnica.getData() != null ? schedaTecnica.getData() : Date.valueOf(LocalDate.now()));
        schedaTecnica.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));
        if(schedaTecnica.getId() == null){
            schedaTecnica.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        }else {
            schedaTecnicaRepository.findById(schedaTecnica.getId()).ifPresent(s -> schedaTecnica.setDataInserimento(s.getDataInserimento()));
        }

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

}
package com.contarbn.service;

import com.contarbn.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ScheduleService {

    private final ScontoService scontoService;
    private final OrdineClienteService ordineClienteService;
    private final GiacenzaArticoloService giacenzaArticoloService;
    private final GiacenzaIngredienteService giacenzaIngredienteService;
    private final EtichettaService etichettaService;

    @Scheduled(cron = "${job.compute-giacenze.cron}", zone = "Europe/Rome")
    public void computeGiacenze() {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("Executing compute Giacenze");
        try{
            giacenzaArticoloService.computeGiacenzaBulk(null);

            watch.stop();
            log.info("Executed compute Giacenze in {} sec", watch.getTotalTimeSeconds());
        } catch(Exception e){
            watch.stop();
            log.error("Error", e);
            log.error("Error executing compute Giacenze. {} sec", watch.getTotalTimeSeconds());
        }
    }

    @Scheduled(cron = "${job.delete-etichette.cron}", zone = "Europe/Rome")
    public void deleteEtichette(){
        log.info("Executing remove of old Etichette");
        List<Etichetta> etichette = etichettaService.getEtichetteToDelete(1);
        etichette.forEach(e -> etichettaService.delete(e.getUuid()));
        log.info("Executed remove of old Etichette");
    }

    @Scheduled(cron = "${job.delete-giacenze.cron}", zone = "Europe/Rome")
    public void deleteGiacenzeExpiredZero(){
        log.info("Executing remove of expired and zero Giacenze");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Date to check: {}", yesterday);

        Set<GiacenzaArticolo> giacenzeArticoli = giacenzaArticoloService.getAllNotAggregate();
        if(giacenzeArticoli != null && !giacenzeArticoli.isEmpty()){
            for(GiacenzaArticolo giacenzaArticolo : giacenzeArticoli){
                Articolo articolo = giacenzaArticolo.getArticolo();
                if(articolo != null){
                    UnitaMisura unitaMisura = articolo.getUnitaMisura();
                    if(unitaMisura != null){
                        if("pz".equals(unitaMisura.getNome())){
                            if (giacenzaArticolo.getPezzi() != null && giacenzaArticolo.getPezzi().equals(0f)) {
                                if (giacenzaArticolo.getScadenza() != null) {
                                    if (giacenzaArticolo.getScadenza().toLocalDate().compareTo(yesterday)<0) {
                                        giacenzaArticoloService.delete(giacenzaArticolo.getId());
                                    }
                                } else {
                                    giacenzaArticoloService.delete(giacenzaArticolo.getId());
                                }
                            }
                        } else {
                            if (giacenzaArticolo.getQuantita() != null && giacenzaArticolo.getQuantita().equals(0f)) {
                                if (giacenzaArticolo.getScadenza() != null) {
                                    if (giacenzaArticolo.getScadenza().toLocalDate().compareTo(yesterday)<0) {
                                        giacenzaArticoloService.delete(giacenzaArticolo.getId());
                                    }
                                } else {
                                    giacenzaArticoloService.delete(giacenzaArticolo.getId());
                                }
                            }
                        }
                    }
                }
            }
        }
        log.info("Deleted Giacenze Articoli");

        Set<GiacenzaIngrediente> giacenzeIngredienti = giacenzaIngredienteService.getAllNotAggregate();
        if(giacenzeIngredienti != null && !giacenzeIngredienti.isEmpty()){
            giacenzeIngredienti.stream()
                    .filter(g -> (g.getQuantita().equals(0f) && g.getScadenza() == null)||(g.getQuantita().equals(0f) && g.getScadenza() != null && g.getScadenza().toLocalDate().compareTo(yesterday)<0))
                    .forEach(g -> giacenzaIngredienteService.delete(g.getId()));
        }
        log.info("Deleted Giacenze Ingredienti");
        log.info("Executed remove of expired and zero Giacenze");
    }


    @Scheduled(cron = "${job.delete-ordini-clienti.cron}", zone = "Europe/Rome")
    public void deleteOrdiniClientiEvasiAndExpired(){
        log.info("Executing remove of expired and evasi Ordini Clienti");
        Set<OrdineCliente> expiredAndEvasiOrdiniClienti = ordineClienteService.getOrdiniClientiEvasiAndExpired(1);
        expiredAndEvasiOrdiniClienti.forEach(oc -> ordineClienteService.delete(oc.getId()));
        log.info("Executed remove of expired and evasi Ordini Clienti");
    }

    @Scheduled(cron = "${job.delete-sconti.cron}", zone = "Europe/Rome")
    public void deleteScontiExpired() {
        log.info("Executing remove of expired Sconti");
        Date now = new Date(System.currentTimeMillis());
        List<Long> expiredSconti = scontoService.getAll().stream().filter(s -> (s.getDataAl() != null && s.getDataAl().before(now))).map(Sconto::getId).collect(Collectors.toList());
        expiredSconti.forEach(scontoService::delete);
        log.info("Executed remove of expired Sconti");
    }

    /*
    @Scheduled(cron = "0 35 17 * * ?")
    public void deleteExpiredTelefonate(){
        log.info("Executing remove of expired Telefonate");
        LocalDateTime limitDatetime = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0);
        log.info("Date to check: {}", limitDatetime);
        List<Telefonata> telefonate = telefonataService.getAll();
        if(!telefonate.isEmpty()){
            telefonate.stream()
                    .filter(t -> t.getDataEsecuzione().toLocalDateTime().isBefore(limitDatetime))
                    .forEach(t -> telefonataService.delete(t.getId()));
        }
        log.info("Executed remove of expired Telefonate");
    }
    */

}

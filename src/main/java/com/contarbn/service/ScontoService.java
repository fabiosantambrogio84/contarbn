package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Articolo;
import com.contarbn.model.Sconto;
import com.contarbn.repository.ScontoRepository;
import com.contarbn.util.enumeration.Resource;
import com.contarbn.util.enumeration.TipologiaSconto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScontoService {

    private final ScontoRepository scontoRepository;

    private final ArticoloService articoloService;

    public List<Sconto> getAll(){
        log.info("Retrieving the list of 'sconti'");
        List<Sconto> sconti = scontoRepository.findAll();
        log.info("Retrieved {} 'sconti'", sconti.size());
        return sconti;
    }

    public List<Sconto> getValidSconti(Resource resource, Long idResource, Date data){
        Predicate<Sconto> isScontoDataDaLessOrEquals = sconto -> {
            if(data != null && sconto.getDataDal() != null){
                return sconto.getDataDal().compareTo(data)<=0;
            }
            return true;
        };
        Predicate<Sconto> isScontoDataAGreaterOrEquals = sconto -> {
            if(data != null && sconto.getDataAl() != null){
                return sconto.getDataAl().compareTo(data)>=0;
            }
            return true;
        };

        log.info("Retrieving the list of valid  'sconti' for {} '{}' and date '{}'", resource.getLabel(), idResource, data);

        // retrieve the Sconti of tipologia ARTICOLO valid at date
        List<Sconto> validScontiArticoli = new ArrayList<>();
        if(resource.equals(Resource.CLIENTE)){
            validScontiArticoli = scontoRepository.findByTipologiaAndClienteId(TipologiaSconto.ARTICOLO.name(), idResource)
                    .stream()
                    .filter(isScontoDataDaLessOrEquals.and(isScontoDataAGreaterOrEquals))
                    .collect(Collectors.toList());
        } else if(resource.equals(Resource.FORNITORE)){
            validScontiArticoli = scontoRepository.findByTipologiaAndFornitoreId(TipologiaSconto.ARTICOLO.name(), idResource)
                    .stream()
                    .filter(isScontoDataDaLessOrEquals.and(isScontoDataAGreaterOrEquals))
                    .collect(Collectors.toList());
        }


        log.info("Valid Sconti Articolo size {}", validScontiArticoli.size());

        // retrieve the Sconti of tipologia FORNITORE valid at date (without Articolo valued)
        List<Sconto> validScontiFornitoriWithoutArticoli = new ArrayList<>();
        if(resource.equals(Resource.CLIENTE)){
            validScontiFornitoriWithoutArticoli = scontoRepository.findByTipologiaAndClienteId(TipologiaSconto.FORNITORE.name(), idResource)
                    .stream()
                    .filter(isScontoDataDaLessOrEquals.and(isScontoDataAGreaterOrEquals))
                    .collect(Collectors.toList());
        } else if(resource.equals(Resource.FORNITORE)){
            validScontiFornitoriWithoutArticoli = scontoRepository.findByTipologiaAndFornitoreId(TipologiaSconto.FORNITORE.name(), idResource)
                    .stream()
                    .filter(isScontoDataDaLessOrEquals.and(isScontoDataAGreaterOrEquals))
                    .collect(Collectors.toList());
        }

        log.info("Valid Sconti Fornitore (without id_articolo) size {}", validScontiFornitoriWithoutArticoli.size());

        // create the list of valid Sconti of tipologia FORNITORE with Articolo valued
        List<Sconto> validScontiFornitori = new ArrayList<>();
        validScontiFornitoriWithoutArticoli.forEach(sf -> {
            Set<Articolo> articoliByFornitore = articoloService.getAllByAttivoAndFornitoreId(true, sf.getFornitore().getId());
            articoliByFornitore.forEach(a -> {
                Sconto sconto = new Sconto();
                sconto.setId(sf.getId());
                sconto.setCliente(sf.getCliente());
                sconto.setDataInserimento(sf.getDataInserimento());
                sconto.setDataDal(sf.getDataDal());
                sconto.setDataAl(sf.getDataAl());
                sconto.setFornitore(sf.getFornitore());
                sconto.setTipologia(sf.getTipologia());
                sconto.setValore(sf.getValore());
                sconto.setArticolo(a);
                validScontiFornitori.add(sconto);
            });
        });
        log.info("Valid Sconti Fornitore (with id_articolo) size {}", validScontiFornitori.size());

        List<Sconto> validScontiArticoloFornitore = Stream.concat(validScontiArticoli.stream(), validScontiFornitori.stream()).collect(Collectors.toList());

        log.info("Valid Sconti Articolo and Fornitore size {}", validScontiArticoloFornitore.size());

        // create a map with key Articolo.id and value the list of associated Sconti
        Map<Long, List<Sconto>> articoloScontiMap = new HashMap<>();
        validScontiArticoloFornitore.forEach(sa -> {
            Articolo articolo = sa.getArticolo();
            if(articolo != null){
                articoloScontiMap.computeIfAbsent(articolo.getId(), v -> new ArrayList<>());
                articoloScontiMap.computeIfPresent(articolo.getId(), (key, value) -> {
                    value.add(sa);
                    return value;
                });
            }
        });

        Comparator<Sconto> compareByDataInserimento = (s1, s2) -> {
            Long d1 = s1.getDataInserimento().getTime();
            Long d2 = s2.getDataInserimento().getTime();
            return d2.compareTo(d1);
        };

        List<Sconto> sconti = new ArrayList<>();
        // iterate over map and get the valid prior Sconto for each Articolo
        for (Map.Entry<Long, List<Sconto>> entry : articoloScontiMap.entrySet()) {
            List<Sconto> articoloSconti = entry.getValue();

            Optional<Sconto> optionalSconto = articoloSconti.stream()
                    .filter(s -> s.getTipologia().equals(TipologiaSconto.ARTICOLO.name())).min(compareByDataInserimento);
            if(optionalSconto.isPresent()){
                sconti.add(optionalSconto.get());
            } else {
                articoloSconti.stream()
                        .filter(s -> s.getTipologia().equals(TipologiaSconto.FORNITORE.name())).min(compareByDataInserimento).ifPresent(sconti::add);
            }
        }

        log.info("Retrieved {} 'sconti'", sconti.size());
        return sconti;
    }

    public Sconto getOne(Long scontoId){
        log.info("Retrieving 'sconto' '{}'", scontoId);
        Sconto sconto = scontoRepository.findById(scontoId).orElseThrow(ResourceNotFoundException::new);
        log.info("Retrieved 'sconto' '{}'", sconto);
        return sconto;
    }

    public List<Sconto> create(List<Sconto> sconti){
        log.info("Creating 'sconti'");
        sconti.forEach(s -> {
            s.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
            Sconto createdSconto = scontoRepository.save(s);
            log.info("Created 'sconto' '{}'", createdSconto);
        });

        return sconti;
    }

    public Sconto update(Sconto sconto){
        log.info("Updating 'sconto'");
        Sconto scontoCurrent = scontoRepository.findById(sconto.getId()).orElseThrow(ResourceNotFoundException::new);
        sconto.setDataInserimento(scontoCurrent.getDataInserimento());
        Sconto updatedSconto = scontoRepository.save(sconto);
        log.info("Updated 'sconto' '{}'", updatedSconto);
        return updatedSconto;
    }

    public void delete(Long scontoId){
        log.info("Deleting 'sconto' '{}'", scontoId);
        scontoRepository.deleteById(scontoId);
        log.info("Deleted 'sconto' '{}'", scontoId);
    }

    public List<Sconto> getAllByTipologia(String tipologia){
        log.info("Retrieving the list of 'sconti' filtered by tipologia '{}'", tipologia);
        List<Sconto> sconti = scontoRepository.findByTipologia(tipologia);
        log.info("Retrieved {} 'sconti'", sconti.size());
        return sconti;
    }

}

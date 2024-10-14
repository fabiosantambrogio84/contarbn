package com.contarbn.service;

import com.contarbn.model.*;
import com.contarbn.model.stats.*;
import com.contarbn.util.Utils;
import com.contarbn.util.enumeration.StatisticaOpzione;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class StatisticaService {

    private final DdtService ddtService;
    private final FatturaAccompagnatoriaService fatturaAccompagnatoriaService;
    private final RicevutaPrivatoService ricevutaPrivatoService;

    public Statistica computeStatistiche(StatisticaFilter statisticaFilter){
        log.info("Computing statistiche");
        Statistica statistica = new Statistica();
        statistica.setTotaleVenduto(BigDecimal.ZERO);
        statistica.setTotaleQuantitaVenduta(BigDecimal.ZERO);
        statistica.setNumeroRighe(0);
        
        List<DdtArticolo> filteredDdtArticoli = filterDdtArticoli(statisticaFilter);
        List<FatturaAccompagnatoriaArticolo> filteredFattureAccompagnatorieArticoli = filterFattureAccompagnatorieArticoli(statisticaFilter);
        List<RicevutaPrivatoArticolo> filteredRicevutePrivatoArticoli = filterRicevutePrivatoArticoli(statisticaFilter);

        if(!isNullOrEmpty(filteredDdtArticoli) || !isNullOrEmpty(filteredFattureAccompagnatorieArticoli) || !isNullOrEmpty(filteredRicevutePrivatoArticoli)){
            ComputationObject computationObject = new ComputationObject();
            computationObject.setDdtArticoli(filteredDdtArticoli);
            computationObject.setFattureAccompagnatorieArticoli(filteredFattureAccompagnatorieArticoli);
            computationObject.setRicevutePrivatoArticoli(filteredRicevutePrivatoArticoli);

            BigDecimal totaleVenduto = computeTotaleVenduto(computationObject);
            Float totaleQuantitaVenduta = computeTotaleQuantitaVenduta(computationObject);
            long numeroRighe = computeNumeroRighe(computationObject);

            statistica.setTotaleVenduto(Utils.roundPrice(totaleVenduto));
            statistica.setTotaleQuantitaVenduta(Utils.roundPrice(new BigDecimal(totaleQuantitaVenduta)));
            statistica.setNumeroRighe(Long.valueOf(numeroRighe).intValue());

            StatisticaOpzione opzione = statisticaFilter.getOpzione();
            if(opzione != null){
                if(opzione.equals(StatisticaOpzione.MOSTRA_DETTAGLIO)){
                    statistica.setStatisticaArticoli(createStatisticaArticoli(computationObject));
                } else {
                    statistica.setStatisticaArticoloGroups(createStatisticaArticoliGroups(computationObject));
                }
            }
        }

        log.info("Statistica: {}", statistica.toString());
        log.info("Statistiche successfully computed");
        return statistica;
    }

    private List<DdtArticolo> filterDdtArticoli(StatisticaFilter statisticaFilter){
        log.info("Retrieving 'ddts articoli' for statistiche computation...");
        List<Ddt> filteredDdts = new ArrayList<>();
        List<DdtArticolo> filteredDdtArticoli = new ArrayList<>();

        log.info("Retrieving 'ddts' with 'data' >= {} ...", statisticaFilter.getDataDal());
        List<Ddt> ddts = ddtService.getByDataGreaterThanEqual(statisticaFilter.getDataDal());
        log.info("Retrieved {} 'ddts' with 'data' >= {}", ddts.size(), statisticaFilter.getDataDal());

        if(!ddts.isEmpty()) {
            Date dataA = statisticaFilter.getDataAl();
            List<Long> idsClienti = statisticaFilter.getIdsClienti();

            log.info("Filtering 'ddts' by 'dataAl' <= {} and 'idsClienti' in {}  ...", dataA, idsClienti);

            Predicate<Ddt> isDdtDataALessOrEquals = ddt -> {
                if (dataA != null) {
                    return ddt.getData().compareTo(dataA) <= 0;
                }
                return true;
            };

            Predicate<Ddt> isDdtClientiIn = ddt -> {
                if (idsClienti != null && !idsClienti.isEmpty()) {
                    Cliente cliente = ddt.getCliente();
                    if (cliente != null) {
                        return idsClienti.contains(cliente.getId());
                    }
                }
                return true;
            };

            filteredDdts = ddts.stream().filter(isDdtDataALessOrEquals
                    .and(isDdtClientiIn)).collect(Collectors.toList());
        }
        log.info("Retrieved {} filtered 'ddts' for statistiche computation", filteredDdts.size());

        if(!filteredDdts.isEmpty()){
            Long idFornitore = statisticaFilter.getIdFornitore();
            List<Long> idsArticoli = statisticaFilter.getIdsArticoli();

            log.info("Filtering 'ddts articoli' by 'idFornitore' = {} and 'idsArticoli' in {}  ...", idFornitore, idsArticoli);

            Set<DdtArticolo> ddtArticoli = filteredDdts.stream().flatMap(ddt -> ddt.getDdtArticoli().stream()).collect(Collectors.toSet());

            Predicate<DdtArticolo> isDdtArticoloFornitoreEquals = ddtArticolo -> {
                if (idFornitore != null) {
                    Articolo articolo = ddtArticolo.getArticolo();
                    if(articolo != null){
                        Fornitore fornitore = articolo.getFornitore();
                        if(fornitore != null){
                            return fornitore.getId().equals(idFornitore);
                        }
                    }
                }
                return true;
            };

            Predicate<DdtArticolo> isDdtArticoloArticoliIn = ddtArticolo -> {
                if (idsArticoli != null && !idsArticoli.isEmpty()) {
                    Articolo articolo = ddtArticolo.getArticolo();
                    if(articolo != null){
                        return idsArticoli.contains(articolo.getId());
                    }
                }
                return true;
            };

            filteredDdtArticoli = ddtArticoli.stream().filter(isDdtArticoloFornitoreEquals.and(isDdtArticoloArticoliIn)).collect(Collectors.toList());

        }
        log.info("Retrieved {} filtered 'ddts articoli' for statistiche computation", filteredDdtArticoli.size());

        return filteredDdtArticoli;
    }

    private List<FatturaAccompagnatoriaArticolo> filterFattureAccompagnatorieArticoli(StatisticaFilter statisticaFilter){
        log.info("Retrieving 'fattureAccompagnatorieArticoli' for statistiche computation...");
        List<FatturaAccompagnatoria> filteredFattureAccompagnatorie = new ArrayList<>();
        List<FatturaAccompagnatoriaArticolo> filteredFattureAccompagnatorieArticoli = new ArrayList<>();

        log.info("Retrieving 'fattureAccompagnatorie' with 'data' >= {} ...", statisticaFilter.getDataDal());
        List<FatturaAccompagnatoria> fattureAccompagnatorie = fatturaAccompagnatoriaService.getByDataGreaterThanEqual(statisticaFilter.getDataDal());
        log.info("Retrieved {} 'fattureAccompagnatorie' with 'data' >= {}", fattureAccompagnatorie.size(), statisticaFilter.getDataDal());

        if(!fattureAccompagnatorie.isEmpty()) {
            Date dataA = statisticaFilter.getDataAl();
            List<Long> idsClienti = statisticaFilter.getIdsClienti();

            log.info("Filtering 'fattureAccompagnatorie' by 'dataAl' <= {} and 'idsClienti' in {}  ...", dataA, idsClienti);

            Predicate<FatturaAccompagnatoria> isFatturaAccompagnatoriaDataALessOrEquals = fatturaAccompagnatoria -> {
                if (dataA != null) {
                    return fatturaAccompagnatoria.getData().compareTo(dataA) <= 0;
                }
                return true;
            };

            Predicate<FatturaAccompagnatoria> isFatturaAccompagnatoriaClientiIn = fatturaAccompagnatoria -> {
                if (idsClienti != null && !idsClienti.isEmpty()) {
                    Cliente cliente = fatturaAccompagnatoria.getCliente();
                    if (cliente != null) {
                        return idsClienti.contains(cliente.getId());
                    }
                }
                return true;
            };

            filteredFattureAccompagnatorie = fattureAccompagnatorie.stream().filter(isFatturaAccompagnatoriaDataALessOrEquals
                    .and(isFatturaAccompagnatoriaClientiIn)).collect(Collectors.toList());
        }

        log.info("Retrieved {} filtered'fattureAccompagnatorie' for statistiche computation", filteredFattureAccompagnatorie.size());

        if(!filteredFattureAccompagnatorie.isEmpty()){
            Long idFornitore = statisticaFilter.getIdFornitore();
            List<Long> idsArticoli = statisticaFilter.getIdsArticoli();

            log.info("Filtering 'fattureAccompagnatorieArticoli' by 'idFornitore' = {} and 'idsArticoli' in {}  ...", idFornitore, idsArticoli);

            Set<FatturaAccompagnatoriaArticolo> fattureAccompagnatorieArticoli = filteredFattureAccompagnatorie.stream().flatMap(fatturaAccompagnatoria -> fatturaAccompagnatoria.getFatturaAccompagnatoriaArticoli().stream()).collect(Collectors.toSet());

            Predicate<FatturaAccompagnatoriaArticolo> isFatturaAccompagnatoriaArticoloFornitoreEquals = fatturaAccompagnatoria -> {
                if (idFornitore != null) {
                    Articolo articolo = fatturaAccompagnatoria.getArticolo();
                    if(articolo != null){
                        Fornitore fornitore = articolo.getFornitore();
                        if(fornitore != null){
                            return fornitore.getId().equals(idFornitore);
                        }
                    }
                }
                return true;
            };

            Predicate<FatturaAccompagnatoriaArticolo> isFatturaAccompagnatoriaArticoliIn = FatturaAccompagnatoriaArticolo -> {
                if (idsArticoli != null && !idsArticoli.isEmpty()) {
                    Articolo articolo = FatturaAccompagnatoriaArticolo.getArticolo();
                    if(articolo != null){
                        return idsArticoli.contains(articolo.getId());
                    }
                }
                return true;
            };

            filteredFattureAccompagnatorieArticoli = fattureAccompagnatorieArticoli.stream().filter(isFatturaAccompagnatoriaArticoloFornitoreEquals.and(isFatturaAccompagnatoriaArticoliIn)).collect(Collectors.toList());
        }
        log.info("Retrieved {} filtered 'fattureAccompagnatorieArticoli' for statistiche computation", filteredFattureAccompagnatorieArticoli.size());

        return filteredFattureAccompagnatorieArticoli;
    }

    private List<RicevutaPrivatoArticolo> filterRicevutePrivatoArticoli(StatisticaFilter statisticaFilter){
        log.info("Retrieving 'ricevutePrivato articoli' for statistiche computation...");
        List<RicevutaPrivato> filteredRicevutePrivato = new ArrayList<>();
        List<RicevutaPrivatoArticolo> filterRicevutePrivatoArticoli = new ArrayList<>();

        log.info("Retrieving 'ricevutePrivato' with 'data' >= {} ...", statisticaFilter.getDataDal());
        List<RicevutaPrivato> ricevutePrivato = ricevutaPrivatoService.getByDataGreaterThanEqual(statisticaFilter.getDataDal());
        log.info("Retrieved {} 'ricevutePrivato' with 'data' >= {}", ricevutePrivato.size(), statisticaFilter.getDataDal());

        if(!ricevutePrivato.isEmpty()) {
            Date dataA = statisticaFilter.getDataAl();
            List<Long> idsClienti = statisticaFilter.getIdsClienti();

            log.info("Filtering 'ricevutePrivato' by 'dataAl' <= {} and 'idsClienti' in {}  ...", dataA, idsClienti);

            Predicate<RicevutaPrivato> isRicevutaPrivatoDataALessOrEquals = ricevutaPrivato -> {
                if (dataA != null) {
                    return ricevutaPrivato.getData().compareTo(dataA) <= 0;
                }
                return true;
            };

            Predicate<RicevutaPrivato> isRicevutaPrivatoClientiIn = ricevutaPrivato -> {
                if (idsClienti != null && !idsClienti.isEmpty()) {
                    Cliente cliente = ricevutaPrivato.getCliente();
                    if (cliente != null) {
                        return idsClienti.contains(cliente.getId());
                    }
                }
                return true;
            };

            filteredRicevutePrivato = ricevutePrivato.stream().filter(isRicevutaPrivatoDataALessOrEquals
                    .and(isRicevutaPrivatoClientiIn)).collect(Collectors.toList());
        }
        log.info("Retrieved {} filtered 'ricevutePrivato' for statistiche computation", filteredRicevutePrivato.size());

        if(!filteredRicevutePrivato.isEmpty()){
            Long idFornitore = statisticaFilter.getIdFornitore();
            List<Long> idsArticoli = statisticaFilter.getIdsArticoli();

            log.info("Filtering 'ricevutePrivato articoli' by 'idFornitore' = {} and 'idsArticoli' in {}  ...", idFornitore, idsArticoli);

            Set<RicevutaPrivatoArticolo> ricevutePrivatoArticoli = filteredRicevutePrivato.stream().flatMap(ricevutaPrivato -> ricevutaPrivato.getRicevutaPrivatoArticoli().stream()).collect(Collectors.toSet());

            Predicate<RicevutaPrivatoArticolo> isRicevutaPrivatoArticoloFornitoreEquals = ricevutaPrivatoArticolo -> {
                if (idFornitore != null) {
                    Articolo articolo = ricevutaPrivatoArticolo.getArticolo();
                    if(articolo != null){
                        Fornitore fornitore = articolo.getFornitore();
                        if(fornitore != null){
                            return fornitore.getId().equals(idFornitore);
                        }
                    }
                }
                return true;
            };

            Predicate<RicevutaPrivatoArticolo> isRicevutaPrivatoArticoloArticoliIn = ricevutaPrivatoArticolo -> {
                if (idsArticoli != null && !idsArticoli.isEmpty()) {
                    Articolo articolo = ricevutaPrivatoArticolo.getArticolo();
                    if(articolo != null){
                        return idsArticoli.contains(articolo.getId());
                    }
                }
                return true;
            };

            filterRicevutePrivatoArticoli = ricevutePrivatoArticoli.stream().filter(isRicevutaPrivatoArticoloFornitoreEquals.and(isRicevutaPrivatoArticoloArticoliIn)).collect(Collectors.toList());

        }
        log.info("Retrieved {} filtered 'ricevutePrivato articoli' for statistiche computation", filterRicevutePrivatoArticoli.size());

        return filterRicevutePrivatoArticoli;
    }
    
    private BigDecimal computeTotaleVenduto(ComputationObject computationObject){
        BigDecimal totaleVenduto;

        BigDecimal totaleVendutoDdts = computationObject.getDdtArticoli().stream().map(DdtArticolo::getTotale).reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Totale venduto ddts {}", totaleVendutoDdts);

        BigDecimal totaleVendutoFattureAccompagnatorie = computationObject.getFattureAccompagnatorieArticoli().stream().map(FatturaAccompagnatoriaArticolo::getTotale).reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Totale venduto fatture accompagnatorie {}", totaleVendutoFattureAccompagnatorie);

        BigDecimal totaleVendutoRicevutePrivato = computationObject.getRicevutePrivatoArticoli().stream().map(RicevutaPrivatoArticolo::getTotale).reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Totale venduto ricevute privato {}", totaleVendutoRicevutePrivato);

        totaleVenduto = totaleVendutoDdts.add(totaleVendutoFattureAccompagnatorie).add(totaleVendutoRicevutePrivato);
        log.info("Totale venduto {}", totaleVenduto);
        return totaleVenduto;
    }

    private Float computeTotaleQuantitaVenduta(ComputationObject computationObject){
        Float totaleQuantitaVenduta;

        Float totaleQuantitaVendutaDdts = computationObject.getDdtArticoli().stream().map(DdtArticolo::getQuantita).reduce(0f, (fa1, fa2) -> (fa1 != null ? fa1 : 0f) + (fa2 != null ? fa2 : 0f));
        log.info("Totale quantita venduta ddts {}", totaleQuantitaVendutaDdts);

        Float totaleQuantitaVendutaFattureAccompagnatorie = computationObject.getFattureAccompagnatorieArticoli().stream().map(FatturaAccompagnatoriaArticolo::getQuantita).reduce(0f, (fa1, fa2) -> (fa1 != null ? fa1 : 0f) + (fa2 != null ? fa2 : 0f));
        log.info("Totale quantita venduta fatture accompagnatorie {}", totaleQuantitaVendutaFattureAccompagnatorie);

        Float totaleQuantitaVendutaRicevutePrivato = computationObject.getRicevutePrivatoArticoli().stream().map(RicevutaPrivatoArticolo::getQuantita).reduce(0f, (fa1, fa2) -> (fa1 != null ? fa1 : 0f) + (fa2 != null ? fa2 : 0f));
        log.info("Totale quantita venduta ricevute privato {}", totaleQuantitaVendutaRicevutePrivato);

        totaleQuantitaVenduta = totaleQuantitaVendutaDdts + totaleQuantitaVendutaFattureAccompagnatorie + totaleQuantitaVendutaRicevutePrivato;
        log.info("Totale quantita venduta {}", totaleQuantitaVenduta);
        return totaleQuantitaVenduta;
    }

    private long computeNumeroRighe(ComputationObject computationObject){

        long numeroRigheDdts = computationObject.getDdtArticoli().size();
        log.info("Numero righe ddts {}", numeroRigheDdts);

        long numeroRigheFattureAccompagnatorie = computationObject.getFattureAccompagnatorieArticoli().size();
        log.info("Numero righe fatture accompagnatorie {}", numeroRigheFattureAccompagnatorie);

        long numeroRigheRicevutePrivato = computationObject.getRicevutePrivatoArticoli().size();
        log.info("Numero righe ricevute privato {}", numeroRigheRicevutePrivato);

        long numeroRighe = numeroRigheDdts + numeroRigheFattureAccompagnatorie + numeroRigheRicevutePrivato;
        log.info("Numero righe {}", numeroRighe);
        return numeroRighe;
    }

    private List<StatisticaArticolo> createStatisticaArticoli(ComputationObject computationObject){
        log.info("Computing 'statistica mostra dettaglio'...");
        List<StatisticaArticolo> statisticaArticoli = new ArrayList<>();

        for(DdtArticolo ddtArticolo: computationObject.getDdtArticoli()){
            StatisticaArticolo statisticaArticolo = new StatisticaArticoloBuilder()
                    .setTipologia("DDT")
                    .setIdArticolo(ddtArticolo.getId().getArticoloId())
                    .setProgressivo(ddtArticolo.getDdt().getProgressivo())
                    .setCodice(ddtArticolo.getArticolo().getCodice())
                    .setDescrizione(ddtArticolo.getArticolo().getDescrizione())
                    .setLotto(ddtArticolo.getLotto())
                    .setPrezzo(ddtArticolo.getPrezzo())
                    .setQuantita(ddtArticolo.getQuantita())
                    .setTotale(ddtArticolo.getTotale())
                    .build();

            statisticaArticoli.add(statisticaArticolo);
        }

        for(FatturaAccompagnatoriaArticolo fatturaAccompagnatoriaArticolo: computationObject.getFattureAccompagnatorieArticoli()){
            StatisticaArticolo statisticaArticolo = new StatisticaArticoloBuilder()
                    .setTipologia("FATTURA_ACCOMPAGNATORIA")
                    .setIdArticolo(fatturaAccompagnatoriaArticolo.getId().getArticoloId())
                    .setProgressivo(fatturaAccompagnatoriaArticolo.getFatturaAccompagnatoria().getProgressivo())
                    .setCodice(fatturaAccompagnatoriaArticolo.getArticolo().getCodice())
                    .setDescrizione(fatturaAccompagnatoriaArticolo.getArticolo().getDescrizione())
                    .setLotto(fatturaAccompagnatoriaArticolo.getLotto())
                    .setPrezzo(fatturaAccompagnatoriaArticolo.getPrezzo())
                    .setQuantita(fatturaAccompagnatoriaArticolo.getQuantita())
                    .setTotale(fatturaAccompagnatoriaArticolo.getTotale())
                    .build();

            statisticaArticoli.add(statisticaArticolo);
        }

        for(RicevutaPrivatoArticolo ricevutaPrivatoArticolo: computationObject.getRicevutePrivatoArticoli()){
            StatisticaArticolo statisticaArticolo = new StatisticaArticoloBuilder()
                    .setTipologia("RICEVUTA_PRIVATO")
                    .setIdArticolo(ricevutaPrivatoArticolo.getId().getArticoloId())
                    .setProgressivo(ricevutaPrivatoArticolo.getRicevutaPrivato().getProgressivo())
                    .setCodice(ricevutaPrivatoArticolo.getArticolo().getCodice())
                    .setDescrizione(ricevutaPrivatoArticolo.getArticolo().getDescrizione())
                    .setLotto(ricevutaPrivatoArticolo.getLotto())
                    .setPrezzo(ricevutaPrivatoArticolo.getPrezzo())
                    .setQuantita(ricevutaPrivatoArticolo.getQuantita())
                    .setTotale(ricevutaPrivatoArticolo.getTotale())
                    .build();

            statisticaArticoli.add(statisticaArticolo);
        }

        statisticaArticoli.sort(Comparator.comparing(StatisticaArticolo::getProgressivo).reversed()
                .thenComparing(StatisticaArticolo::getCodice));

        log.info("Computed 'statistica mostra dettaglio'");

        return statisticaArticoli;
    }

    private List<StatisticaArticoloGroup> createStatisticaArticoliGroups(ComputationObject computationObject){
        log.info("Computing 'statistica raggruppa dettaglio'...");
        List<StatisticaArticoloGroup> statisticaArticoliGroups = new ArrayList<>();

        List<StatisticaArticolo> statisticaArticoli = createStatisticaArticoli(computationObject);

        Map<Long, List<StatisticaArticolo>> statisticaArticoliMap = statisticaArticoli.stream().collect(Collectors.groupingBy(StatisticaArticolo::getIdArticolo));

        for (Map.Entry<Long, List<StatisticaArticolo>> entry : statisticaArticoliMap.entrySet()) {
            List<StatisticaArticolo> statisticaArticoliByArticolo = entry.getValue();

            String codice = statisticaArticoliByArticolo.stream().map(StatisticaArticolo::getCodice).findFirst().get();
            String descrizione = statisticaArticoliByArticolo.stream().map(StatisticaArticolo::getDescrizione).findFirst().get();
            int numRighe = statisticaArticoliByArticolo.size();
            BigDecimal totVenduto = statisticaArticoliByArticolo.stream().map(StatisticaArticolo::getTotale).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totQuantitaVenduta = statisticaArticoliByArticolo.stream().map(sa -> new BigDecimal(sa.getQuantita())).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totVendutoMedio = totVenduto.divide(new BigDecimal(numRighe), 3, RoundingMode.HALF_UP);

            StatisticaArticoloGroup statisticaArticoloGroup = new StatisticaArticoloGroupBuilder()
                    .setCodice(codice)
                    .setDescrizione(descrizione)
                    .setNumeroRighe(numRighe)
                    .setTotaleQuantitaVenduta(Utils.roundPrice(totQuantitaVenduta))
                    .setTotaleVenduto(Utils.roundPrice(totVenduto))
                    .setTotaleVendutoMedio(Utils.roundPrice(totVendutoMedio))
                    .build();

            statisticaArticoliGroups.add(statisticaArticoloGroup);
        }

        statisticaArticoliGroups.sort(Comparator.comparing(StatisticaArticoloGroup::getCodice));

        log.info("Computed 'statistica raggruppa dettaglio'");
        return statisticaArticoliGroups;
    }

    private static boolean isNullOrEmpty(List<?> list){
        return list == null || list.isEmpty();
    }

}

package com.contarbn.service;

import com.contarbn.exception.ResourceAlreadyExistingException;
import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.*;
import com.contarbn.repository.FatturaAccompagnatoriaRepository;
import com.contarbn.repository.PagamentoRepository;
import com.contarbn.util.Utils;
import com.contarbn.util.enumeration.Operation;
import com.contarbn.util.enumeration.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FatturaAccompagnatoriaService {

    private final FatturaAccompagnatoriaRepository fatturaAccompagnatoriaRepository;
    private final FatturaAccompagnatoriaArticoloService fatturaAccompagnatoriaArticoloService;
    private final FatturaAccompagnatoriaTotaleService fatturaAccompagnatoriaTotaleService;
    private final StatoFatturaService statoFatturaService;
    private final TipoFatturaService tipoFatturaService;
    private final GiacenzaArticoloService giacenzaArticoloService;
    private final PagamentoRepository pagamentoRepository;

    public Set<FatturaAccompagnatoria> getAll(){
        log.info("Retrieving the list of 'fatture accompagnatorie'");
        Set<FatturaAccompagnatoria> fattureAccompagnatorie = fatturaAccompagnatoriaRepository.findAllByOrderByAnnoDescProgressivoDesc();
        log.info("Retrieved {} 'fatture accompagnatorie'", fattureAccompagnatorie.size());
        return fattureAccompagnatorie;
    }

    public FatturaAccompagnatoria getOne(Long fatturaAccompagnatoriaId){
        log.info("Retrieving 'fattura accompagnatoria' '{}'", fatturaAccompagnatoriaId);
        FatturaAccompagnatoria fatturaAccompagnatoria = fatturaAccompagnatoriaRepository.findById(fatturaAccompagnatoriaId).orElseThrow(ResourceNotFoundException::new);
        log.info("Retrieved 'fattura accompagnatoria' '{}'", fatturaAccompagnatoria);
        return fatturaAccompagnatoria;
    }

    public Map<String, Integer> getAnnoAndProgressivo(){
        Integer anno = ZonedDateTime.now().getYear();
        Integer progressivo = getProgressivo(anno);
        HashMap<String, Integer> result = new HashMap<>();
        result.put("anno", anno);
        result.put("progressivo", progressivo);

        return result;
    }

    private Integer getProgressivo(Integer anno){
        Integer progressivo = 1;
        Integer resultProgressivo = fatturaAccompagnatoriaRepository.getLastProgressivoByAnnoContabile(anno);
        if(resultProgressivo != null){
            progressivo = resultProgressivo + 1;
        }
        return progressivo;
    }

    public List<FatturaAccompagnatoria> getByDataGreaterThanEqual(Date data){
        log.info("Retrieving 'fattureAccompagnatorie' with 'data' greater or equals to '{}'", data);
        List<FatturaAccompagnatoria> fattureAccompagnatorie = fatturaAccompagnatoriaRepository.findByDataGreaterThanEqualOrderByProgressivoDesc(data);
        log.info("Retrieved {} 'fattureAccompagnatorie' with 'data' greater or equals to '{}'", fattureAccompagnatorie.size(), data);
        return fattureAccompagnatorie;
    }

    public Map<Cliente, List<FatturaAccompagnatoria>> getFattureAccompagnatorieByCliente(Date dateFrom, Date dateTo){
        log.info("Retrieving the list of fatture_acompagnatorie, grouped by cliente, with speditoAde 'false', dateFrom '{}' and dateTo '{}'", dateFrom, dateTo);

        Map<Cliente, List<FatturaAccompagnatoria>> fattureAccompagnatorieByCliente = new HashMap<>();

        //Predicate<FatturaAccompagnatoria> isFatturaAccompagnatoriaSpeditoAdeFalse = fattura -> fattura.getSpeditoAde().equals(Boolean.FALSE);

        Predicate<FatturaAccompagnatoria> isFatturaAccompagnatoriaDataDaGreaterOrEquals = fattura -> {
            if(dateFrom != null){
                return fattura.getData().compareTo(dateFrom)>=0;
            }
            return true;
        };
        Predicate<FatturaAccompagnatoria> isFatturaAccompagnatoriaDataALessOrEquals = fattura -> {
            if(dateTo != null){
                return fattura.getData().compareTo(dateTo)<=0;
            }
            return true;
        };

        Set<FatturaAccompagnatoria> fattureAccompagnatorie = fatturaAccompagnatoriaRepository.findAll().stream()
                .filter(isFatturaAccompagnatoriaDataDaGreaterOrEquals
                .and(isFatturaAccompagnatoriaDataALessOrEquals)).collect(Collectors.toSet());

        if(fattureAccompagnatorie != null && !fattureAccompagnatorie.isEmpty()){
            for(FatturaAccompagnatoria fatturaAccompagnatoria : fattureAccompagnatorie){
                Cliente cliente = fatturaAccompagnatoria.getCliente();

                List<FatturaAccompagnatoria> fattureAccompagnatorieList = fattureAccompagnatorieByCliente.getOrDefault(cliente, new ArrayList<>());
                fattureAccompagnatorieList.add(fatturaAccompagnatoria);

                fattureAccompagnatorieByCliente.put(cliente, fattureAccompagnatorieList);
            }
        }

        log.info("Successfully retrieved the list of fatture_acompagnatorie grouped by cliente");

        return fattureAccompagnatorieByCliente;
    }

    @Transactional
    public FatturaAccompagnatoria create(FatturaAccompagnatoria fatturaAccompagnatoria){
        log.info("Creating 'fattura accompagnatoria'");

        Integer progressivo = fatturaAccompagnatoria.getProgressivo();
        if(progressivo == null){
            progressivo = getProgressivo(fatturaAccompagnatoria.getAnno());
            fatturaAccompagnatoria.setProgressivo(progressivo);
        }

        checkExistsByAnnoAndProgressivoAndIdNot(fatturaAccompagnatoria.getAnno(),fatturaAccompagnatoria.getProgressivo(), -1L);

        fatturaAccompagnatoria.setStatoFattura(statoFatturaService.getDaPagare());
        fatturaAccompagnatoria.setTipoFattura(tipoFatturaService.getAccompagnatoria());
        fatturaAccompagnatoria.setSpeditoAde(false);
        fatturaAccompagnatoria.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));

        log.info(fatturaAccompagnatoria.getScannerLog());

        FatturaAccompagnatoria createdFatturaAccompagnatoria = fatturaAccompagnatoriaRepository.save(fatturaAccompagnatoria);

        createdFatturaAccompagnatoria.getFatturaAccompagnatoriaArticoli().stream().forEach(faa -> {
            if(faa.getQuantita() != null && faa.getQuantita() != 0 && faa.getPrezzo() != null){
                faa.getId().setFatturaAccompagnatoriaId(createdFatturaAccompagnatoria.getId());
                faa.getId().setUuid(UUID.randomUUID().toString());
                fatturaAccompagnatoriaArticoloService.create(faa);

                // compute 'giacenza articolo'
                giacenzaArticoloService.computeGiacenza(faa.getId().getArticoloId(), faa.getLotto(), faa.getScadenza());
            } else {
                log.info("FatturaAccompagnatoriaArticolo not saved because quantity null or zero ({}) or prezzo zero ({})", faa.getQuantita(), faa.getPrezzo());
            }
        });

        createdFatturaAccompagnatoria.getFatturaAccompagnatoriaTotali().stream().forEach(fat -> {
            fat.getId().setFatturaAccompagnatoriaId(createdFatturaAccompagnatoria.getId());
            fat.getId().setUuid(UUID.randomUUID().toString());
            fatturaAccompagnatoriaTotaleService.create(fat);
        });

        computeTotali(createdFatturaAccompagnatoria, createdFatturaAccompagnatoria.getFatturaAccompagnatoriaArticoli(), createdFatturaAccompagnatoria.getFatturaAccompagnatoriaTotali());

        fatturaAccompagnatoriaRepository.save(createdFatturaAccompagnatoria);
        log.info("Created 'fattura accompagnatoria' '{}'", createdFatturaAccompagnatoria);

        return createdFatturaAccompagnatoria;
    }

    @Transactional
    public FatturaAccompagnatoria patch(Map<String,Object> patchFatturaAccompagnatoria){
        log.info("Patching 'fatturaAccompagnatoria'");

        Long id = Long.valueOf((Integer) patchFatturaAccompagnatoria.get("id"));
        FatturaAccompagnatoria fatturaAccompagnatoria = fatturaAccompagnatoriaRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        patchFatturaAccompagnatoria.forEach((key, value) -> {
            if(key.equals("id")){
                fatturaAccompagnatoria.setId(Long.valueOf((Integer)value));
            } else if(key.equals("speditoAde")){
                if(value != null){
                    fatturaAccompagnatoria.setSpeditoAde((boolean)value);
                } else {
                    fatturaAccompagnatoria.setSpeditoAde(Boolean.FALSE);
                }
            }
        });
        FatturaAccompagnatoria patchedFatturaAccompagnatoria = fatturaAccompagnatoriaRepository.save(fatturaAccompagnatoria);

        log.info("Patched 'fatturaAccompagnatoria' '{}'", patchedFatturaAccompagnatoria);
        return patchedFatturaAccompagnatoria;
    }

    @Transactional
    public void patchSpeditoAdeFattureAccompagnatorieByCliente(Map<Cliente, List<FatturaAccompagnatoria>> fattureAccompagnatorieByCliente, boolean speditoAde){
        log.info("Updating fatture accompagnatorie setting speditoAde='{}'", speditoAde);

        if(fattureAccompagnatorieByCliente != null && !fattureAccompagnatorieByCliente.isEmpty()){
            for(Cliente cliente : fattureAccompagnatorieByCliente.keySet()){
                for(FatturaAccompagnatoria fatturaAccompagnatoria : fattureAccompagnatorieByCliente.get(cliente)){
                    Map<String, Object> patchFatturaAccompagnatoria = new HashMap<>();
                    patchFatturaAccompagnatoria.put("id", fatturaAccompagnatoria.getId());
                    patchFatturaAccompagnatoria.put("speditoAde", speditoAde);

                    patch(patchFatturaAccompagnatoria);
                }
            }
        }

        log.info("Successfully updated fatture accompagnatorie setting speditoAde={}", speditoAde);
    }

    @Transactional
    public void delete(Long fatturaAccompagnatoriaId){
        log.info("Deleting 'fattura accompagnatoria' '{}'", fatturaAccompagnatoriaId);

        FatturaAccompagnatoria fatturaAccompagnatoria = getOne(fatturaAccompagnatoriaId);

        Set<FatturaAccompagnatoriaArticolo> fatturaAccompagnatoriaArticoli = fatturaAccompagnatoriaArticoloService.findByFatturaAccompagnatoriaId(fatturaAccompagnatoriaId);

        pagamentoRepository.deleteByFatturaAccompagnatoriaId(fatturaAccompagnatoriaId);
        fatturaAccompagnatoriaArticoloService.deleteByFatturaAccompagnatoriaId(fatturaAccompagnatoriaId);
        fatturaAccompagnatoriaTotaleService.deleteByFatturaAccompagnatoriaId(fatturaAccompagnatoriaId);
        fatturaAccompagnatoriaRepository.deleteById(fatturaAccompagnatoriaId);

        for (FatturaAccompagnatoriaArticolo fatturaAccompagnatoriaArticolo:fatturaAccompagnatoriaArticoli) {

            giacenzaArticoloService.createMovimentazioneManualeArticolo(fatturaAccompagnatoriaArticolo.getId().getArticoloId(), fatturaAccompagnatoriaArticolo.getLotto(), fatturaAccompagnatoriaArticolo.getScadenza(), fatturaAccompagnatoriaArticolo.getNumeroPezzi(), fatturaAccompagnatoriaArticolo.getQuantita(), Operation.DELETE, Resource.FATTURA_ACCOMPAGNATORIA, fatturaAccompagnatoria.getId(), String.valueOf(fatturaAccompagnatoria.getProgressivo()), fatturaAccompagnatoria.getAnno(), null);

            giacenzaArticoloService.computeGiacenza(fatturaAccompagnatoriaArticolo.getId().getArticoloId(), fatturaAccompagnatoriaArticolo.getLotto(), fatturaAccompagnatoriaArticolo.getScadenza());
        }

        log.info("Deleted 'fattura accompagnatoria' '{}'", fatturaAccompagnatoriaId);
    }

    private void checkExistsByAnnoAndProgressivoAndIdNot(Integer anno, Integer progressivo, Long idFattura){
        Optional<FatturaAccompagnatoria> fatturaAccompagnatoria = fatturaAccompagnatoriaRepository.findByAnnoAndProgressivoAndIdNot(anno, progressivo, idFattura);
        if(fatturaAccompagnatoria.isPresent()){
            throw new ResourceAlreadyExistingException(Resource.FATTURA_ACCOMPAGNATORIA, anno, progressivo);
        }
    }

    private void computeTotali(FatturaAccompagnatoria fatturaAccompagnatoria, Set<FatturaAccompagnatoriaArticolo> fatturaAccompagnatoriaArticoli, Set<FatturaAccompagnatoriaTotale> fatturaAccompagnatoriaTotali){
        Map<AliquotaIva, Set<FatturaAccompagnatoriaArticolo>> ivaFatturaAccompagnatoriaArticoliMap = new HashMap<>();
        fatturaAccompagnatoriaArticoli.stream().forEach(faa -> {
            Articolo articolo = fatturaAccompagnatoriaArticoloService.getArticolo(faa);
            AliquotaIva iva = articolo.getAliquotaIva();
            Set<FatturaAccompagnatoriaArticolo> fatturaAccompagnatoriaArticoliByIva;
            if(ivaFatturaAccompagnatoriaArticoliMap.containsKey(iva)){
                fatturaAccompagnatoriaArticoliByIva = ivaFatturaAccompagnatoriaArticoliMap.get(iva);
            } else {
                fatturaAccompagnatoriaArticoliByIva = new HashSet<>();
            }
            fatturaAccompagnatoriaArticoliByIva.add(faa);
            ivaFatturaAccompagnatoriaArticoliMap.put(iva, fatturaAccompagnatoriaArticoliByIva);
        });
        Float totaleQuantita = 0f;
        BigDecimal totale = new BigDecimal(0);
        for (Map.Entry<AliquotaIva, Set<FatturaAccompagnatoriaArticolo>> entry : ivaFatturaAccompagnatoriaArticoliMap.entrySet()) {
            BigDecimal iva = entry.getKey().getValore();
            BigDecimal totaleByIva = new BigDecimal(0);
            Set<FatturaAccompagnatoriaArticolo> fatturaAccompagnatoriaArticoliByIva = entry.getValue();
            for(FatturaAccompagnatoriaArticolo fatturaAccompagnatoriaArticolo: fatturaAccompagnatoriaArticoliByIva){
                BigDecimal imponibile = fatturaAccompagnatoriaArticolo.getImponibile() != null ? fatturaAccompagnatoriaArticolo.getImponibile() : BigDecimal.ZERO;
                totaleByIva = totaleByIva.add(imponibile);
                totaleQuantita = totaleQuantita + (fatturaAccompagnatoriaArticolo.getQuantita() != null ? fatturaAccompagnatoriaArticolo.getQuantita() : 0f);
            }
            totale = totale.add(totaleByIva.add(totaleByIva.multiply(iva.divide(new BigDecimal(100)))));
        }

        BigDecimal totaleIva;
        BigDecimal totaleImponibile;

        totaleIva = fatturaAccompagnatoriaTotali.stream().map(FatturaAccompagnatoriaTotale::getTotaleIva).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        totaleImponibile = fatturaAccompagnatoriaTotali.stream().map(FatturaAccompagnatoriaTotale::getTotaleImponibile).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        fatturaAccompagnatoria.setTotale(Utils.roundPrice(totale));
        fatturaAccompagnatoria.setTotaleAcconto(new BigDecimal(0));
        fatturaAccompagnatoria.setTotaleQuantita(Utils.roundQuantity(new BigDecimal(totaleQuantita)));
        fatturaAccompagnatoria.setTotaleImponibile(Utils.roundPrice(totaleImponibile));
        fatturaAccompagnatoria.setTotaleImponibile(Utils.roundPrice(totaleIva));
    }
}

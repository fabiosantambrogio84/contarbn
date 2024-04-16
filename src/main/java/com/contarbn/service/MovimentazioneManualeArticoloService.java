package com.contarbn.service;

import com.contarbn.model.Articolo;
import com.contarbn.model.GiacenzaArticolo;
import com.contarbn.model.MovimentazioneManualeArticolo;
import com.contarbn.repository.MovimentazioneManualeArticoloRepository;
import com.contarbn.util.enumeration.Operation;
import com.contarbn.util.enumeration.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class MovimentazioneManualeArticoloService {

    private final MovimentazioneManualeArticoloRepository movimentazioneManualeArticoloRepository;

    @Transactional
    public MovimentazioneManualeArticolo create(GiacenzaArticolo giacenzaArticolo){
        log.info("Creating 'movimentazione manuale articolo'");

        MovimentazioneManualeArticolo createdMovimentazioneManualeArticolo = null;
        if(giacenzaArticolo.getArticolo() != null){
            MovimentazioneManualeArticolo movimentazioneManualeArticolo = new MovimentazioneManualeArticolo();
            movimentazioneManualeArticolo.setArticolo(giacenzaArticolo.getArticolo());
            movimentazioneManualeArticolo.setLotto(giacenzaArticolo.getLotto());
            movimentazioneManualeArticolo.setScadenza(giacenzaArticolo.getScadenza());
            movimentazioneManualeArticolo.setPezzi(giacenzaArticolo.getPezzi());
            movimentazioneManualeArticolo.setQuantita(giacenzaArticolo.getQuantita());
            movimentazioneManualeArticolo.setOperation(Operation.CREATE.name());
            movimentazioneManualeArticolo.setContext(Resource.GIACENZA_ARTICOLO.name());
            movimentazioneManualeArticolo.setCompute(Boolean.TRUE);
            movimentazioneManualeArticolo.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
            movimentazioneManualeArticolo.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));

            createdMovimentazioneManualeArticolo = movimentazioneManualeArticoloRepository.save(movimentazioneManualeArticolo);

            log.info("Created 'movimentazione manuale articolo' '{}'", createdMovimentazioneManualeArticolo);

        } else {
            log.error("Error creating 'movimentazione manuale articolo': articolo null");
        }
        return createdMovimentazioneManualeArticolo;
    }

    public void create(Long idArticolo, String lotto, Date scadenza, Integer pezzi, Float quantita, Operation operation, Resource resource, Long idDocumento, String numDocumento, Integer annoDocumento, String fornitoreDocumento){
        log.info("Creating 'movimentazione manuale articolo' operation {} on {}", operation.name(), resource.name());

        if(idArticolo != null){
            MovimentazioneManualeArticolo movimentazioneManualeArticolo = new MovimentazioneManualeArticolo();
            Articolo articolo = new Articolo();
            articolo.setId(idArticolo);
            movimentazioneManualeArticolo.setArticolo(articolo);
            movimentazioneManualeArticolo.setLotto(lotto);
            movimentazioneManualeArticolo.setScadenza(scadenza);
            movimentazioneManualeArticolo.setPezzi(pezzi);
            movimentazioneManualeArticolo.setQuantita(quantita);
            movimentazioneManualeArticolo.setOperation(operation.name());
            movimentazioneManualeArticolo.setContext(resource.name());
            if(Operation.CREATE.equals(operation)){
                movimentazioneManualeArticolo.setCompute(Boolean.TRUE);
            } else {
                movimentazioneManualeArticolo.setCompute(Boolean.FALSE);
            }
            movimentazioneManualeArticolo.setIdDocumento(idDocumento);
            movimentazioneManualeArticolo.setNumDocumento(numDocumento);
            movimentazioneManualeArticolo.setAnnoDocumento(annoDocumento);
            if(StringUtils.isNotEmpty(fornitoreDocumento)){
                movimentazioneManualeArticolo.setFornitoreDocumento(fornitoreDocumento);
            }
            movimentazioneManualeArticolo.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
            movimentazioneManualeArticolo.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));

            movimentazioneManualeArticoloRepository.save(movimentazioneManualeArticolo);
        } else {
            log.error("Error creating 'movimentazione manuale articolo': id_articolo null");
        }
    }

    @Transactional
    public void deleteByArticoloIdIn(List<Long> idArticoli){
        log.info("Bulk deleting all the 'movimentazioni manuali articolo' by 'idArticolo' (number of elements to delete: {})", idArticoli.size());
        movimentazioneManualeArticoloRepository.deleteByArticoloIdIn(idArticoli);
        log.info("Bulk deleted all the specified 'movimentazioni manuali articolo");
    }

    public Set<MovimentazioneManualeArticolo> getByArticoloIdAndLottoAndScadenza(Long idArticolo, String lotto, Date scadenza, Timestamp dataAggiornamento){
        log.info("Retrieving 'movimentazioni manuali articoli' by 'idArticolo' '{}', 'lotto' '{}' and 'scadenza' '{}'", idArticolo, lotto, scadenza);
        Set<MovimentazioneManualeArticolo> movimentazioniManualiArticoli = movimentazioneManualeArticoloRepository.findByArticoloIdAndLotto(idArticolo, lotto);
        if(movimentazioniManualiArticoli != null && !movimentazioniManualiArticoli.isEmpty()){
            if(scadenza != null){
                movimentazioniManualiArticoli = movimentazioniManualiArticoli.stream()
                        .filter(mma -> (mma.getScadenza() != null && mma.getScadenza().toLocalDate().compareTo(scadenza.toLocalDate())==0 && mma.getCompute())).collect(Collectors.toSet());
            }
            if(dataAggiornamento != null){
                movimentazioniManualiArticoli = movimentazioniManualiArticoli.stream()
                        .filter(mma -> mma.getDataAggiornamento().after(dataAggiornamento)).collect(Collectors.toSet());
            }

        }
        log.info("Retrieved '{}' 'movimentazioni manuali articoli'", movimentazioniManualiArticoli.size());
        return movimentazioniManualiArticoli;
    }

    public Optional<MovimentazioneManualeArticolo> findLastByOperationAndContext(Operation operation, Resource resource, Long idArticolo, String lotto, Date scadenza){
        return movimentazioneManualeArticoloRepository.findLastByOperationAndContext(operation, resource, idArticolo, lotto, scadenza);
    }

}

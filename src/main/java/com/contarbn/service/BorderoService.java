package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Bordero;
import com.contarbn.model.BorderoRiga;
import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VBorderoRiga;
import com.contarbn.repository.BorderoDetailRepository;
import com.contarbn.repository.BorderoRepository;
import com.contarbn.repository.BorderoRigaRepository;
import com.contarbn.repository.custom.VBorderoRigaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class BorderoService {

    private final BorderoRepository borderoRepository;
    private final BorderoRigaRepository borderoRigaRepository;
    private final BorderoDetailRepository borderoDetailRepository;

    public Long create(String autista, Date dataConsegna){
        log.info("Creating 'bordero'");

        String[] autistaSplit = StringUtils.split(autista, '_');
        Integer idAutistaTrasportatore = Integer.valueOf(autistaSplit[1]);

        Bordero bordero = new Bordero();
        if(autistaSplit[0].equals("autista")){
            bordero.setIdAutista(idAutistaTrasportatore);
        } else {
            bordero.setIdTrasportatore(idAutistaTrasportatore);
        }
        bordero.setDataConsegna(dataConsegna);
        bordero.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));

        borderoRepository.save(bordero);

        log.info("Creating 'bordero detail' and 'bordero righe'");
        String result = borderoRepository.generaBordero(bordero.getId(), idAutistaTrasportatore, dataConsegna);
        if(!result.equals("OK")){
            log.error("Error creating 'bordero detail' and 'bordero righe': {}", result);
            return null;
        }

        return bordero.getId();
    }

    public VBorderoRiga patch(Map<String,Object> patchBorderoRiga){
        log.info("Patching 'bordero riga'");

        String uuid = (String) patchBorderoRiga.get("uuid");
        BorderoRiga borderoRiga = borderoRigaRepository.findById(uuid).orElseThrow(ResourceNotFoundException::new);
        patchBorderoRiga.forEach((key, value) -> {
            if ("progressivo".equals(key)) {
                borderoRiga.setProgressivo(Integer.parseInt((String)value));
            }
        });
        BorderoRiga patchedBorderoRiga = borderoRigaRepository.save(borderoRiga);

        log.info("Patched 'bordero riga' '{}'", patchedBorderoRiga);
        return borderoRigaRepository.findByIdBorderoRiga(uuid);
    }

    public void delete(Timestamp dataInserimento){
        log.info("Deleting 'bordero' and 'bordero righe' with data_inserimento less than {})", dataInserimento);

        List<Bordero> borderoList = borderoRepository.findByDataInserimento(dataInserimento);
        if(!borderoList.isEmpty()){
            borderoList.forEach(b-> {
                borderoDetailRepository.deleteByIdBordero(b.getId().intValue());
                borderoRigaRepository.deleteByIdBordero(b.getId().intValue());
                borderoRepository.deleteById(b.getId());
            });
        }
        log.info("Deleted 'bordero' and 'bordero righe'");
    }

    public void deleteBorderoRiga(String uuid){
        borderoRigaRepository.deleteById(uuid);
    }

    public List<VBorderoRiga> getAllByIdBordero(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, Long idBordero){
        log.info("Retrieving the list of 'bordero righe' filtered by request parameters");
        List<VBorderoRiga> borderoRighe = borderoRigaRepository.findByIdBordero(draw, start, length, sortOrders, idBordero);
        log.info("Retrieved {} 'bordero righe'", borderoRighe.size());
        return borderoRighe;
    }

    public Integer getCountByIdBordero(Long idBordero){
        log.info("Retrieving the count of 'bordero righe' filtered by request parameters");
        Integer count = borderoRigaRepository.countByIdBordero(idBordero);
        log.info("Retrieved {} 'bordero righe'", count);
        return count;
    }

}

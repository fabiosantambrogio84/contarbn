package com.contarbn.service;

import com.contarbn.model.AliquotaIva;
import com.contarbn.model.NotaAccreditoRiga;
import com.contarbn.repository.NotaAccreditoRigaRepository;
import com.contarbn.util.AccountingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Set;

@Service
public class NotaAccreditoRigaService {

    private static Logger LOGGER = LoggerFactory.getLogger(NotaAccreditoRigaService.class);

    private final NotaAccreditoRigaRepository notaAccreditoRigaRepository;

    private final AliquotaIvaService aliquotaIvaService;

    @Autowired
    public NotaAccreditoRigaService(final NotaAccreditoRigaRepository notaAccreditoRigaRepository, final AliquotaIvaService aliquotaIvaService){
        this.notaAccreditoRigaRepository = notaAccreditoRigaRepository;
        this.aliquotaIvaService = aliquotaIvaService;
    }

    public Set<NotaAccreditoRiga> findAll(){
        LOGGER.info("Retrieving the list of 'nota accredito riga'");
        Set<NotaAccreditoRiga> notaAccreditoRiga = notaAccreditoRigaRepository.findAll();
        LOGGER.info("Retrieved {} 'nota accredito riga'", notaAccreditoRiga.size());
        return notaAccreditoRiga;
    }

    public NotaAccreditoRiga create(NotaAccreditoRiga notaAccreditoRiga){
        LOGGER.info("Creating 'nota accredito riga'");
        notaAccreditoRiga.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        notaAccreditoRiga.setImponibile(computeImponibile(notaAccreditoRiga));
        notaAccreditoRiga.setTotale(computeTotale(notaAccreditoRiga));

        NotaAccreditoRiga createdNotaAccreditoRiga = notaAccreditoRigaRepository.save(notaAccreditoRiga);
        LOGGER.info("Created 'nota accredito riga' '{}'", createdNotaAccreditoRiga);
        return createdNotaAccreditoRiga;
    }

    public void deleteByNotaAccreditoId(Long notaAccreditoId){
        LOGGER.info("Deleting 'nota accredito riga' by 'nota accredito' '{}'", notaAccreditoId);
        notaAccreditoRigaRepository.deleteByNotaAccreditoId(notaAccreditoId);
        LOGGER.info("Deleted 'nota accredito riga' by 'nota accredito' '{}'", notaAccreditoId);
    }

    public AliquotaIva getAliquotaIva(NotaAccreditoRiga notaAccreditoRiga){
        return aliquotaIvaService.getOne(notaAccreditoRiga.getAliquotaIva().getId());
    }

    private BigDecimal computeImponibile(NotaAccreditoRiga notaAccreditoRiga){
        return AccountingUtils.computeImponibile(notaAccreditoRiga.getQuantita(), notaAccreditoRiga.getPrezzo(), notaAccreditoRiga.getSconto());
    }

    private BigDecimal computeTotale(NotaAccreditoRiga notaAccreditoRiga){
        AliquotaIva aliquotaIva = getAliquotaIva(notaAccreditoRiga);
        return AccountingUtils.computeTotale(notaAccreditoRiga.getQuantita(), notaAccreditoRiga.getPrezzo(), notaAccreditoRiga.getSconto(), aliquotaIva);
    }

}

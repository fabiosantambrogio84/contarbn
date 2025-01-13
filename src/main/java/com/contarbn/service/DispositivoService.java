package com.contarbn.service;

import com.contarbn.exception.OperationException;
import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Dispositivo;
import com.contarbn.repository.DispositivoRepository;
import com.contarbn.util.Utils;
import com.contarbn.util.enumeration.TipologiaDispositivo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class DispositivoService {

    private final DispositivoRepository dispositivoRepository;

    public Set<Dispositivo> getAll(){
        log.info("Retrieving the list of 'dispositivi'");
        Set<Dispositivo> dispositivi = dispositivoRepository.findAll();
        log.info("Retrieved {} 'dispositivi'", dispositivi.size());
        return dispositivi;
    }

    public Dispositivo getOne(Long idDispositivo){
        return dispositivoRepository.findById(idDispositivo).orElseThrow(ResourceNotFoundException::new);
    }

    public List<Dispositivo> getByTipoAndAttivo(TipologiaDispositivo tipologiaDispositivo, Boolean attivo){
        log.info("Retrieving the list of 'dispositivo {}'", tipologiaDispositivo.name());
        return dispositivoRepository.findByTipoAndAttivo(tipologiaDispositivo.name(), Utils.getActiveValues(attivo));
    }

    public Dispositivo create(Dispositivo dispositivo){
        log.info("Creating 'dispositivo' {}", dispositivo);

        check(dispositivo.getTipo(), -1L, dispositivo.getNome(), dispositivo.getIp(), dispositivo.getPorta(), dispositivo.getPredefinito());

        dispositivo.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        dispositivo.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));

        return dispositivoRepository.save(dispositivo);
    }

    public Dispositivo update(Dispositivo dispositivo){
        log.info("Updating 'dispositivo'");

        check(dispositivo.getTipo(), dispositivo.getId(), dispositivo.getNome(), dispositivo.getIp(), dispositivo.getPorta(), dispositivo.getPredefinito());

        Dispositivo currentDispositivo = dispositivoRepository.findById(dispositivo.getId()).orElseThrow(ResourceNotFoundException::new);
        dispositivo.setDataInserimento(currentDispositivo.getDataInserimento());
        dispositivo.setDataAggiornamento(Timestamp.from(ZonedDateTime.now().toInstant()));

        Dispositivo updatedDispositivo = dispositivoRepository.save(dispositivo);
        log.info("Updated 'dispositivo' '{}'", updatedDispositivo);
        return updatedDispositivo;
    }

    public void ping(Long idDispositivo) throws IOException {
        Dispositivo dispositivo = getOne(idDispositivo);

        String ip = dispositivo.getIp();
        if(!StringUtils.isEmpty(ip)){
            int timeout = 30000;

            InetAddress address = InetAddress.getByName(ip);

            if (address.isReachable(timeout)) {
                log.info("Dispositivo with ip '{}' is reachable", ip);
            } else {
                String errorMessage = "Dispositivo with ip '"+ip+"' is not reachable after "+timeout+" millis";
                log.error(errorMessage);
                throw new OperationException(errorMessage);
            }
        }
    }

    private void check(TipologiaDispositivo tipo, Long id, String name, String ip, Integer porta, Boolean predefinito){
        if(dispositivoRepository.findByNomeAndIdNot(name, id).isPresent()){
            throw new OperationException("Dispositivo with name '"+name+"' already exists");
        }

        if(dispositivoRepository.findByIpAndPortaAndIdNot(ip, porta, id) != null){
            throw new OperationException("Dispositivo with ip '"+ip+"' and porta '"+porta+"' already exists");
        }

        if(dispositivoRepository.findByTipoAndPredefinitoAndIdNot(tipo, predefinito, id).isPresent()){
            throw new OperationException("Dispositivo with tipo '"+tipo+"' and predefinito '"+predefinito+"' already exists");
        }
    }

}

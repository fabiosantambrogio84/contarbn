package com.contarbn.repository;

import com.contarbn.model.Anagrafica;
import com.contarbn.model.Dispositivo;
import com.contarbn.util.enumeration.TipologiaAnagrafica;
import com.contarbn.util.enumeration.TipologiaDispositivo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DispositivoRepository extends CrudRepository<Dispositivo, Long> {

    @Override
    Set<Dispositivo> findAll();

    Optional<Dispositivo> findByNomeAndIdNot(String nome, Long id);

    @Query(nativeQuery = true, value = "SELECT * FROM contarbn.dispositivo WHERE ip = ?1 AND porta = ?2 AND id <> ?3 LIMIT 1")
    Dispositivo findByIpAndPortaAndIdNot(String ip, Integer porta, Long id);

    Optional<Dispositivo> findByTipoAndPredefinitoAndIdNot(TipologiaDispositivo tipo, Boolean predefinito, Long id);

    @Query(nativeQuery = true, value = "SELECT * FROM contarbn.dispositivo WHERE tipo = ?1 AND attivo in ?2 ORDER BY nome")
    List<Dispositivo> findByTipoAndAttivo(String tipo, List<Integer> activeValues);

}
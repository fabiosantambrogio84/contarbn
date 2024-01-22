package com.contarbn.repository;

import com.contarbn.model.Anagrafica;
import com.contarbn.util.enumeration.TipologiaAnagrafica;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AnagraficaRepository extends CrudRepository<Anagrafica, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM contarbn.anagrafica WHERE tipo = ?1 AND attivo in ?2 ORDER BY ordine, nome")
    List<Anagrafica> findAllByTipoAndAttivo(String tipo, List<Integer> activeValues);

    Optional<Anagrafica> findByTipoAndOrdine(TipologiaAnagrafica tipo, Integer ordine);

}
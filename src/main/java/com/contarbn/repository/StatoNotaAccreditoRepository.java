package com.contarbn.repository;

import com.contarbn.model.StatoNotaAccredito;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface StatoNotaAccreditoRepository extends CrudRepository<StatoNotaAccredito, Long> {

    Set<StatoNotaAccredito> findAllByOrderByOrdine();

    Optional<StatoNotaAccredito> findByCodice(String codice);

}

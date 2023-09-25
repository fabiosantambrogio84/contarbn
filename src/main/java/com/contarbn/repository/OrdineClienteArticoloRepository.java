package com.contarbn.repository;

import com.contarbn.model.OrdineClienteArticolo;
import com.contarbn.model.OrdineClienteArticoloKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface OrdineClienteArticoloRepository extends CrudRepository<OrdineClienteArticolo, Long> {

    @Override
    Set<OrdineClienteArticolo> findAll();

    Optional<OrdineClienteArticolo> findById(OrdineClienteArticoloKey id);

    Set<OrdineClienteArticolo> findByOrdineClienteId(Long idOrdineCliente);

    Set<OrdineClienteArticolo> findByArticoloId(Long articoloId);

    Set<OrdineClienteArticolo> findByOrdineFornitoreId(Long idOrdineFornitore);

    void deleteByOrdineClienteId(Long ordineClienteId);

    void deleteByArticoloId(Long articoloId);
}

package com.contarbn.repository;

import com.contarbn.model.Sconto;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ScontoRepository extends CrudRepository<Sconto, Long> {

    @Override
    List<Sconto> findAll();

    List<Sconto> findByTipologia(String tipologia);

    List<Sconto> findByTipologiaAndClienteId(String tipologia, Long idCliente);

    List<Sconto> findByTipologiaAndFornitoreId(String tipologia, Long idFornitore);
}

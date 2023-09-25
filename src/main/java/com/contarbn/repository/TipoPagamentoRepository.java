package com.contarbn.repository;

import com.contarbn.model.TipoPagamento;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface TipoPagamentoRepository extends CrudRepository<TipoPagamento, Long> {

    Set<TipoPagamento> findAllByOrderByDescrizione();
}

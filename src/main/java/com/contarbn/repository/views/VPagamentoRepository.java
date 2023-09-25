package com.contarbn.repository.views;

import com.contarbn.model.views.VPagamento;
import com.contarbn.repository.custom.VPagamentoCustomRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface VPagamentoRepository extends CrudRepository<VPagamento, Long>, VPagamentoCustomRepository {

    @Override
    Set<VPagamento> findAll();

}

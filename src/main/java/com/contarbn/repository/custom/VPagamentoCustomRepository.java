package com.contarbn.repository.custom;

import com.contarbn.model.views.VPagamento;

import java.sql.Date;
import java.util.List;

public interface VPagamentoCustomRepository {

    List<VPagamento> findByFilter(String tipologia, Date dataDa, Date dataA, String cliente, String fornitore, Float importo);
}

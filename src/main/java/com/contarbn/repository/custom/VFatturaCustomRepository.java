package com.contarbn.repository.custom;

import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VFattura;

import java.sql.Date;
import java.util.List;

public interface VFatturaCustomRepository {

    List<VFattura> findByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, Date dataDa, Date dataA, Integer progressivo, Float importo, String idTipoPagamento, String cliente, Integer idAgente, Integer idArticolo, Integer idStato, Integer idTipo);

    Integer countByFilters(Date dataDa, Date dataA, Integer progressivo, Float importo, String idTipoPagamento, String cliente, Integer idAgente, Integer idArticolo, Integer idStato, Integer idTipo);
}

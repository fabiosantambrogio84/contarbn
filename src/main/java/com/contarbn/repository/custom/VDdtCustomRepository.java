package com.contarbn.repository.custom;

import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VDdt;

import java.sql.Date;
import java.util.List;

public interface VDdtCustomRepository {

    List<VDdt> findByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, Date dataDa, Date dataA, Integer progressivo, Integer idCliente, String cliente, Integer idAgente, Integer idAutista, Integer idStato, Boolean pagato, Boolean fatturato, Float importo, Integer idTipoPagamento, Integer idArticolo);

    Integer countByFilters(Date dataDa, Date dataA, Integer progressivo, Integer idCliente, String cliente, Integer idAgente, Integer idAutista, Integer idStato, Boolean pagato, Boolean fatturato, Float importo, Integer idTipoPagamento, Integer idArticolo);
}

package com.contarbn.repository.custom;

import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VGiacenzaArticolo;

import java.sql.Date;
import java.util.List;

public interface VGiacenzaArticoloCustomRepository {

    List<VGiacenzaArticolo> findByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, String articolo, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto);

    Integer countByFilters(String articolo, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto);
}

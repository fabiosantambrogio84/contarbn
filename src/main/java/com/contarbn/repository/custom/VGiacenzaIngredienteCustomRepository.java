package com.contarbn.repository.custom;

import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VGiacenzaArticolo;
import com.contarbn.model.views.VGiacenzaIngrediente;

import java.sql.Date;
import java.util.List;

public interface VGiacenzaIngredienteCustomRepository {

    List<VGiacenzaIngrediente> findByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, String ingrediente, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto);

    Integer countByFilters(String ingrediente, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto);
}

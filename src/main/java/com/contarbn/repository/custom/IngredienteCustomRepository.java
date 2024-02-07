package com.contarbn.repository.custom;

import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VIngrediente;

import java.util.List;

public interface IngredienteCustomRepository {

    List<VIngrediente> findByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, String codice, String descrizione, Integer idFornitore, Boolean composto, Boolean attivo);

    Integer countByFilters(String codice, String descrizione, Integer idFornitore, Boolean composto, Boolean attivo);
}

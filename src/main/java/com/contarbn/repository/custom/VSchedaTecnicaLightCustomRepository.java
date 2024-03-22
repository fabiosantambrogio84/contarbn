package com.contarbn.repository.custom;

import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VSchedaTecnicaLight;

import java.util.List;

public interface VSchedaTecnicaLightCustomRepository {

    List<VSchedaTecnicaLight> findByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, String prodotto);

    Integer countByFilters(String prodotto);
}

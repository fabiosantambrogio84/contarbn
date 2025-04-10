package com.contarbn.repository.custom;

import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VBorderoRiga;

import java.util.List;

public interface VBorderoRigaRepository {

    List<VBorderoRiga> findByIdBordero(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, Long idBordero);

    Integer countByIdBordero(Long idBordero);

    VBorderoRiga findByIdBorderoRiga(String uuid);
}

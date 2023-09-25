package com.contarbn.repository.custom;

import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VProduzione;

import java.util.List;

public interface VProduzioneCustomRepository {

    List<VProduzione> findByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, Integer codice, String ricetta, String barcodeEan13, String barcodeEan128);

    Integer countByFilters(Integer codice, String ricetta, String barcodeEan13, String barcodeEan128);
}

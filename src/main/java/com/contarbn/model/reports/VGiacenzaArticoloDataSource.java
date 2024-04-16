package com.contarbn.model.reports;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VGiacenzaArticoloDataSource {

    private String articolo;

    private String attivo;

    private String fornitore;

    private String udm;

    private BigDecimal quantita;

    private BigDecimal costo;

    private BigDecimal totale;
}
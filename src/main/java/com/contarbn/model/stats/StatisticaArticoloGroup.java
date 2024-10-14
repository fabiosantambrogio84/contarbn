package com.contarbn.model.stats;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatisticaArticoloGroup {

    private String codice;

    private String descrizione;

    private Integer numeroRighe;

    private BigDecimal totaleVenduto;

    private BigDecimal totaleQuantitaVenduta;

    private BigDecimal totaleVendutoMedio;

    @Override
    public String toString() {

        return "{" +
                "codice: " + codice +
                ", descrizione: " + descrizione +
                ", totaleVenduto: " + totaleVenduto +
                ", totaleQuantitaVenduta: " + totaleQuantitaVenduta +
                ", numeroRighe: " + numeroRighe +
                ", totaleVendutoMedio: " + totaleVendutoMedio +
                "}";
    }
}

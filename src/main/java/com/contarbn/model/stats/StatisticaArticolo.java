package com.contarbn.model.stats;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatisticaArticolo {

    private String tipologia;

    private Long idArticolo;

    private Integer progressivo;

    private String codice;

    private String descrizione;

    private String lotto;

    private Float quantita;

    private BigDecimal prezzo;

    private BigDecimal totale;

    @Override
    public String toString() {

        return "{" +
                "tipologia: " + tipologia +
                "idArticolo: " + idArticolo +
                ", progressivo: " + progressivo +
                ", codice: " + codice +
                ", descrizione: " + descrizione +
                ", lotto: " + lotto +
                ", quantita: " + quantita +
                ", prezzo: " + prezzo +
                ", totale: " + totale +
                "}";

    }
}

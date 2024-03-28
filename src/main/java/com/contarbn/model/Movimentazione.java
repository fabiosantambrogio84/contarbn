package com.contarbn.model;

import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;

@Data
public class Movimentazione {

    private Long idGiacenza;

    private String inputOutput;

    private Date data;

    private Integer pezzi;

    private Float quantita;

    private String descrizione;

    private Timestamp dataInserimento;

    @Override
    public String toString() {

        return "{" +
                "idGiacenza: " + idGiacenza +
                ", inputOutput: " + inputOutput +
                ", data: " + data +
                ", pezzi: " + pezzi +
                ", quantita: " + quantita +
                ", descrizione: " + descrizione +
                ", dataAggiornamento: " + dataInserimento +
                "}";

    }
}

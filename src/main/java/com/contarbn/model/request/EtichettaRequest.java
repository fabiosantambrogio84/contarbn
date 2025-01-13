package com.contarbn.model.request;

import lombok.Data;

import java.sql.Date;

@Data
public class EtichettaRequest {

    private String idProduzioneConfezione;
    private String articolo;
    private String ingredienti;
    private String tracce;
    private String conservazione;
    private String valoriNutrizionali;
    private Date dataConsumazione;
    private String lotto;
    private String peso;
    private String disposizioniComune;
    private String footer;
    private String barcodeEan13;
    private String barcodeEan128;
    private Long idDispositivo;

}

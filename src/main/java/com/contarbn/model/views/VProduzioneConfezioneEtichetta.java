package com.contarbn.model.views;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;

@Data
@EqualsAndHashCode()
@Entity
@Table(name = "v_produzione_confezione_etichetta")
public class VProduzioneConfezioneEtichetta {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "id_produzione")
    private Long idProduzione;

    @Column(name = "lotto")
    private String lotto;

    @Column(name = "scadenza")
    private Date scadenza;

    @Column(name = "articolo")
    private String articolo;

    @Column(name = "ingredienti")
    private String ingredienti;

    @Column(name = "ingredienti_2")
    private String ingredienti2;

    @Column(name = "valori_nutrizionali")
    private String valoriNutrizionali;

    @Column(name = "conservazione")
    private String conservazione;

    @Column(name = "barcode_ean_13")
    private String barcodeEan13;

    @Column(name = "barcode_ean_128")
    private String barcodeEan128;

    @Column(name = "peso_kg")
    private Float pesoKg;

    @Column(name = "lotto_confezione")
    private String lottoConfezione;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", lotto: " + lotto +
                ", scadenza: " + scadenza +
                ", articolo: " + articolo +
                ", ingredienti: " + ingredienti +
                ", ingredienti2: " + ingredienti2 +
                ", valoriNutrizionali: " + valoriNutrizionali +
                ", conservazione: " + conservazione +
                ", barcodeEan13: " + barcodeEan13 +
                ", barcodeEan128: " + barcodeEan128 +
                ", pesoKg: " + pesoKg +
                ", lottoConfezione: " + lottoConfezione +
                "}";
    }
}
package com.contarbn.model.views;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.sql.Date;

@Data
@EqualsAndHashCode
@Entity
@Table(name = "v_scheda_tecnica")
public class VSchedaTecnica {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "id_scheda_tecnica")
    private Long idSchedaTecnica;

    @Column(name = "id_produzione")
    private Long idProduzione;

    @Column(name = "id_articolo")
    private Long idArticolo;

    @Column(name = "id_ricetta")
    private Long idRicetta;

    @Column(name = "num_revisione")
    private Integer numRevisione;

    @Column(name = "anno")
    private Integer anno;

    @Column(name = "data")
    private Date data;

    @Column(name = "codice_prodotto")
    private String codiceProdotto;

    @Column(name = "prodotto")
    private String prodotto;

    @Column(name = "prodotto_2")
    private String prodotto2;

    @Column(name = "peso_netto_confezione")
    private String pesoNettoConfezione;

    @Column(name = "ingredienti")
    private String ingredienti;

    @Column(name = "tracce")
    private String tracce;

    @Column(name = "durata")
    private String durata;

    @Column(name = "conservazione")
    private String conservazione;

    @Column(name = "consigli_consumo")
    private String consigliConsumo;

    @Column(name = "id_tipologia_confezionamento")
    private Long idTipologiaConfezionamento;

    @Column(name = "tipologia_confezionamento")
    private String tipologiaConfezionamento;

    @Column(name = "imballo")
    private String imballo;

    @Column(name = "imballo_dimensioni")
    private String imballoDimensioni;

    @Transient
    final private String objectType = "view";

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", idSchedaTecnica: " + idSchedaTecnica +
                ", idProduzione: " + idProduzione +
                ", idArticolo: " + idArticolo +
                ", idRicetta: " + idRicetta +
                ", numRevisione: " + numRevisione +
                ", anno: " + anno +
                ", data: " + data +
                ", codiceProdotto: " + codiceProdotto +
                ", prodotto: " + prodotto +
                ", prodotto2: " + prodotto2 +
                ", pesoNettoConfezione: " + pesoNettoConfezione +
                ", ingredienti: " + ingredienti +
                ", tracce: " + tracce +
                ", durata: " + durata +
                ", conservazione: " + conservazione +
                ", consigliConsumo: " + consigliConsumo +
                ", idTipologiaConfezionamento: " + idTipologiaConfezionamento +
                ", tipologiaConfezionamento: " + tipologiaConfezionamento +
                ", imballo: " + imballo +
                ", imballoDimensioni: " + imballoDimensioni +
                ", objectType: " + objectType +
                "}";
    }
}
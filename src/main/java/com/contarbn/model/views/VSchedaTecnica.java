package com.contarbn.model.views;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;

@Data
@EqualsAndHashCode
@Entity
@Table(name = "v_scheda_tecnica")
public class VSchedaTecnica {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "id_ricetta")
    private Long idRicetta;

    @Column(name = "id_scheda_tecnica")
    private Long idSchedaTecnica;

    @Column(name = "num_revisione")
    private Integer numRevisione;

    @Column(name = "data")
    private Date data;

    @Column(name = "codice_prodotto")
    private String codiceProdotto;

    @Column(name = "peso_netto_confezione")
    private String pesoNettoConfezione;

    @Column(name = "ingredienti")
    private String ingredienti;

    @Column(name = "allergeni_tracce")
    private String allergeniTracce;

    @Column(name = "durata")
    private String durata;

    @Column(name = "conservazione")
    private String conservazione;

    @Column(name = "consigli_consumo")
    private String consigliConsumo;

    @Column(name = "tipologia_confezionamento")
    private String tipologiaConfezionamento;

    @Column(name = "imballo")
    private String imballo;

    @Column(name = "imballo_dimensioni")
    private String imballoDimensioni;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", idRicetta: " + idRicetta +
                ", idSchedaTecnica: " + idSchedaTecnica +
                ", numRevisione: " + numRevisione +
                ", data: " + data +
                ", codiceProdotto: " + codiceProdotto +
                ", pesoNettoConfezione: " + pesoNettoConfezione +
                ", ingredienti: " + ingredienti +
                ", allergeniTracce: " + allergeniTracce +
                ", durata: " + durata +
                ", conservazione: " + conservazione +
                ", consigliConsumo: " + consigliConsumo +
                ", tipologiaConfezionamento: " + tipologiaConfezionamento +
                ", imballo: " + imballo +
                ", imballoDimensioni: " + imballoDimensioni +
                "}";
    }
}
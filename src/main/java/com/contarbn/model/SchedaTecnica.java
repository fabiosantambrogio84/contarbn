package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(exclude = {"schedaTecnicaNutrienti", "schedaTecnicaAnalisi", "schedaTecnicaRaccolte"})
@Data
@Entity
@Table(name = "scheda_tecnica")
public class SchedaTecnica {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_ricetta")
    private Long idRicetta;

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

    @ManyToOne
    @JoinColumn(name="id_tipologia_confezionamento")
    private Anagrafica tipologiaConfezionamento;

    @ManyToOne
    @JoinColumn(name="id_imballo")
    private Anagrafica imballo;

    @Column(name = "imballo_dimensioni")
    private String imballoDimensioni;

    @Column(name = "data_inserimento")
    private Timestamp dataInserimento;

    @Column(name = "data_aggiornamento")
    private Timestamp dataAggiornamento;

    @OneToMany(mappedBy = "schedaTecnica")
    @JsonIgnoreProperties("schedaTecnica")
    private Set<SchedaTecnicaNutriente> schedaTecnicaNutrienti = new HashSet<>();

    @OneToMany(mappedBy = "schedaTecnica")
    @JsonIgnoreProperties("schedaTecnica")
    private Set<SchedaTecnicaAnalisi> schedaTecnicaAnalisi = new HashSet<>();

    @OneToMany(mappedBy = "schedaTecnica")
    @JsonIgnoreProperties("schedaTecnica")
    private Set<SchedaTecnicaRaccolta> schedaTecnicaRaccolte = new HashSet<>();

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", idRicetta: " + idRicetta +
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
                ", dataInserimento: " + dataInserimento +
                ", dataAggiornamento: " + dataAggiornamento +
                "}";
    }
}

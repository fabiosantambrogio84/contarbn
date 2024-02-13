package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@EqualsAndHashCode
@Data
@Entity
@Table(name = "ricetta_analisi")
public class RicettaAnalisi {

    @EmbeddedId
    RicettaAnalisiKey id;

    @ManyToOne
    @MapsId("id_ricetta")
    @JoinColumn(name = "id_ricetta")
    @JsonIgnoreProperties("ricettaAnalisi")
    private Ricetta ricetta;

    @ManyToOne
    @MapsId("id_analisi")
    @JoinColumn(name = "id_analisi")
    @JsonIgnoreProperties("schedaTecnicaAnalisi")
    private Anagrafica analisi;

    @Column(name = "risultato")
    private String risultato;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", ricetta: " + ricetta +
                ", analisi: " + analisi +
                ", risultato: " + risultato +
                "}";
    }
}
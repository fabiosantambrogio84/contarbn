package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@EqualsAndHashCode
@Data
@Entity
@Table(name = "scheda_tecnica_analisi")
public class SchedaTecnicaAnalisi {

    @EmbeddedId
    SchedaTecnicaAnalisiKey id;

    @ManyToOne
    @MapsId("id_scheda_tecnica")
    @JoinColumn(name = "id_scheda_tecnica")
    @JsonIgnoreProperties("schedaTecnicaAnalisi")
    private SchedaTecnica schedaTecnica;

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
                ", schedaTecnica: " + schedaTecnica +
                ", analisi: " + analisi +
                ", risultato: " + risultato +
                "}";
    }
}
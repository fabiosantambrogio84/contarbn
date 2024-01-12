package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@EqualsAndHashCode
@Data
@Entity
@Table(name = "scheda_tecnica_nutriente")
public class SchedaTecnicaNutriente {

    @EmbeddedId
    SchedaTecnicaNutrienteKey id;

    @ManyToOne
    @MapsId("id_scheda_tecnica")
    @JoinColumn(name = "id_scheda_tecnica")
    @JsonIgnoreProperties("schedaTecnicaNutrienti")
    private SchedaTecnica schedaTecnica;

    @ManyToOne
    @MapsId("id_nutriente")
    @JoinColumn(name = "id_nutriente")
    @JsonIgnoreProperties("schedaTecnicaNutrienti")
    private Anagrafica nutriente;

    @Column(name = "valore")
    private String valore;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", schedaTecnica: " + schedaTecnica +
                ", nutriente: " + nutriente +
                ", valore: " + valore +
                "}";
    }
}

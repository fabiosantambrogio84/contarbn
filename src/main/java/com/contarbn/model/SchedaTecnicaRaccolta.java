package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@EqualsAndHashCode
@Data
@Entity
@Table(name = "scheda_tecnica_raccolta")
public class SchedaTecnicaRaccolta {

    @EmbeddedId
    SchedaTecnicaRaccoltaKey id;

    @ManyToOne
    @MapsId("id_scheda_tecnica")
    @JoinColumn(name = "id_scheda_tecnica")
    @JsonIgnoreProperties("schedaTecnicaRaccolte")
    private SchedaTecnica schedaTecnica;

    @ManyToOne
    @MapsId("id_materiale")
    @JoinColumn(name = "id_materiale")
    @JsonIgnoreProperties("schedaTecnicaRaccolte")
    private Anagrafica materiale;

    @ManyToOne
    @JoinColumn(name="id_raccolta")
    private Anagrafica raccolta;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", schedaTecnica: " + schedaTecnica +
                ", materiale: " + materiale +
                ", raccolta: " + raccolta +
                "}";
    }
}

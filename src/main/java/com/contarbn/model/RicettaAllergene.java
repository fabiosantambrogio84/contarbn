package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ricetta_allergene")
public class RicettaAllergene implements Serializable {

    @EmbeddedId
    RicettaAllergeneKey id;

    @ManyToOne
    @MapsId("id_ricetta")
    @JoinColumn(name = "id_ricetta")
    @JsonIgnoreProperties("ricettaAllergeni")
    private Ricetta ricetta;

    @ManyToOne
    @MapsId("id_allergene")
    @JoinColumn(name = "id_allergene")
    @JsonIgnoreProperties("ricettaAllergeni")
    private Allergene allergene;

    public RicettaAllergeneKey getId() {
        return id;
    }

    public void setId(RicettaAllergeneKey id) {
        this.id = id;
    }

    public Ricetta getRicetta() {
        return ricetta;
    }

    public void setRicetta(Ricetta ricetta) {
        this.ricetta = ricetta;
    }

    public Allergene getAllergene() {
        return allergene;
    }

    public void setAllergene(Allergene allergene) {
        this.allergene = allergene;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("{");
        result.append("ricettaId: " + id.ricettaId);
        result.append(", allergeneId: " + id.allergeneId);
        result.append("}");

        return result.toString();
    }
}

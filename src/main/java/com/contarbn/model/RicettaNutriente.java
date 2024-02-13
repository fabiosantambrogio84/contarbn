package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@EqualsAndHashCode
@Data
@Entity
@Table(name = "ricetta_nutriente")
public class RicettaNutriente {

    @EmbeddedId
    RicettaNutrienteKey id;

    @ManyToOne
    @MapsId("id_ricetta")
    @JoinColumn(name = "id_ricetta")
    @JsonIgnoreProperties("ricettaNutrienti")
    private Ricetta ricetta;

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
                ", ricetta: " + ricetta +
                ", nutriente: " + nutriente +
                ", valore: " + valore +
                "}";
    }
}

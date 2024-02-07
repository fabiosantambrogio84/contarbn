package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "ingrediente_allergene_composizione")
public class IngredienteAllergeneComposizione implements Serializable {

    private static final long serialVersionUID = 1425146456235859138L;

    @EmbeddedId
    IngredienteAllergeneComposizioneKey id;

    @ManyToOne
    @MapsId("id_ingrediente")
    @JoinColumn(name = "id_ingrediente")
    @JsonIgnoreProperties("ingredienteAllergeniComposizione")
    private Ingrediente ingrediente;

    @ManyToOne
    @MapsId("id_allergene")
    @JoinColumn(name = "id_allergene")
    @JsonIgnoreProperties("ingredienteAllergeniComposizione")
    private Allergene allergene;

    @Override
    public String toString() {

        return "{" +
                "ingredienteId: " + id.ingredienteId +
                ", allergeneId: " + id.allergeneId +
                "}";
    }
}

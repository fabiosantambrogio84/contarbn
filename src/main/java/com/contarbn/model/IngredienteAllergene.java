package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ingrediente_allergene")
public class IngredienteAllergene implements Serializable {

    @EmbeddedId
    IngredienteAllergeneKey id;

    @ManyToOne
    @MapsId("id_ingrediente")
    @JoinColumn(name = "id_ingrediente")
    @JsonIgnoreProperties("ingredienteAllergeni")
    private Ingrediente ingrediente;

    @ManyToOne
    @MapsId("id_allergene")
    @JoinColumn(name = "id_allergene")
    @JsonIgnoreProperties("ingredienteAllergeni")
    private Allergene allergene;

    public IngredienteAllergeneKey getId() {
        return id;
    }

    public void setId(IngredienteAllergeneKey id) {
        this.id = id;
    }

    public Ingrediente getIngrediente() {
        return ingrediente;
    }

    public void setIngrediente(Ingrediente ingrediente) {
        this.ingrediente = ingrediente;
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
        result.append("ingredienteId: " + id.ingredienteId);
        result.append(", allergeneId: " + id.allergeneId);
        result.append("}");

        return result.toString();
    }
}

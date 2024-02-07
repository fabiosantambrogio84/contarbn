package com.contarbn.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Data
@EqualsAndHashCode
@Embeddable
public class IngredienteAllergeneComposizioneKey implements Serializable {

    private static final long serialVersionUID = 1837347689794488097L;

    @Column(name = "id_ingrediente")
    Long ingredienteId;

    @Column(name = "id_allergene")
    Long allergeneId;

    public IngredienteAllergeneComposizioneKey(){}

    public IngredienteAllergeneComposizioneKey(Long ingredienteId, Long allergeneId){
        this.ingredienteId = ingredienteId;
        this.allergeneId = allergeneId;
    }

    @Override
    public String toString() {

        return "{" +
                "ingredienteId: " + ingredienteId +
                ", allergeneId: " + allergeneId +
                "}";
    }
}
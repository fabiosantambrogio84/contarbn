package com.contarbn.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class IngredienteAllergeneKey implements Serializable {

    private static final long serialVersionUID = -2379699099773423529L;

    @Column(name = "id_ingrediente")
    Long ingredienteId;

    @Column(name = "id_allergene")
    Long allergeneId;

    public IngredienteAllergeneKey(){}

    public IngredienteAllergeneKey(Long ingredienteId, Long allergeneId){
        this.ingredienteId = ingredienteId;
        this.allergeneId = allergeneId;
    }

    public Long getIngredienteId() {
        return ingredienteId;
    }

    public void setIngredienteId(Long ingredienteId) {
        this.ingredienteId = ingredienteId;
    }

    public Long getAllergeneId() {
        return allergeneId;
    }

    public void setAllergeneId(Long allergeneId) {
        this.allergeneId = allergeneId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredienteId, allergeneId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final IngredienteAllergeneKey that = (IngredienteAllergeneKey) obj;
        return Objects.equals(ingredienteId, that.ingredienteId) &&
                Objects.equals(allergeneId, that.allergeneId);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("{");
        result.append("ingredienteId: " + ingredienteId);
        result.append(", allergeneId: " + allergeneId);
        result.append("}");

        return result.toString();
    }
}
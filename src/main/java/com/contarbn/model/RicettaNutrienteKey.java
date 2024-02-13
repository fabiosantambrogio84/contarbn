package com.contarbn.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Data
@Embeddable
public class RicettaNutrienteKey implements Serializable {

    private static final long serialVersionUID = -8219508358942424290L;

    @Column(name = "id_ricetta")
    Long ricettaId;

    @Column(name = "id_nutriente")
    Long nutrienteId;

    public RicettaNutrienteKey(){}

    public RicettaNutrienteKey(Long ricettaId, Long nutrienteId){
        this.ricettaId = ricettaId;
        this.nutrienteId = nutrienteId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ricettaId, nutrienteId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final RicettaNutrienteKey that = (RicettaNutrienteKey) obj;
        return Objects.equals(ricettaId, that.ricettaId) &&
                Objects.equals(nutrienteId, that.nutrienteId);
    }

    @Override
    public String toString() {

        return "{" +
                "schedaTecnicaId: " + ricettaId +
                ", nutrienteId: " + nutrienteId +
                "}";
    }
}

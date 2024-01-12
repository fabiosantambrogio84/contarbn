package com.contarbn.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Data
@Embeddable
public class SchedaTecnicaNutrienteKey implements Serializable {

    private static final long serialVersionUID = 5121373039405461312L;

    @Column(name = "id_scheda_tecnica")
    Long schedaTecnicaId;

    @Column(name = "id_nutriente")
    Long nutrienteId;

    public SchedaTecnicaNutrienteKey(){}

    public SchedaTecnicaNutrienteKey(Long schedaTecnicaId, Long nutrienteId){
        this.schedaTecnicaId = schedaTecnicaId;
        this.nutrienteId = nutrienteId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedaTecnicaId, nutrienteId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SchedaTecnicaNutrienteKey that = (SchedaTecnicaNutrienteKey) obj;
        return Objects.equals(schedaTecnicaId, that.schedaTecnicaId) &&
                Objects.equals(nutrienteId, that.nutrienteId);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("{");
        result.append("schedaTecnicaId: " + schedaTecnicaId);
        result.append(", nutrienteId: " + nutrienteId);
        result.append("}");

        return result.toString();
    }
}

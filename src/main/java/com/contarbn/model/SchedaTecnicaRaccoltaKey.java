package com.contarbn.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Data
@Embeddable
public class SchedaTecnicaRaccoltaKey implements Serializable {

    private static final long serialVersionUID = -3631264506096293423L;

    @Column(name = "id_scheda_tecnica")
    Long schedaTecnicaId;

    @Column(name = "id_materiale")
    Long materialeId;

    public SchedaTecnicaRaccoltaKey(){}

    public SchedaTecnicaRaccoltaKey(Long schedaTecnicaId, Long materialeId){
        this.schedaTecnicaId = schedaTecnicaId;
        this.materialeId = materialeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedaTecnicaId, materialeId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SchedaTecnicaRaccoltaKey that = (SchedaTecnicaRaccoltaKey) obj;
        return Objects.equals(schedaTecnicaId, that.schedaTecnicaId) &&
                Objects.equals(materialeId, that.materialeId);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("{");
        result.append("schedaTecnicaId: " + schedaTecnicaId);
        result.append(", materialeId: " + materialeId);
        result.append("}");

        return result.toString();
    }
}

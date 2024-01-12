package com.contarbn.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Data
@Embeddable
public class SchedaTecnicaAnalisiKey implements Serializable {

    private static final long serialVersionUID = 4217308118724988060L;

    @Column(name = "id_scheda_tecnica")
    Long schedaTecnicaId;

    @Column(name = "id_analisi")
    Long analisiId;

    public SchedaTecnicaAnalisiKey(){}

    public SchedaTecnicaAnalisiKey(Long schedaTecnicaId, Long analisiId){
        this.schedaTecnicaId = schedaTecnicaId;
        this.analisiId = analisiId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedaTecnicaId, analisiId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SchedaTecnicaAnalisiKey that = (SchedaTecnicaAnalisiKey) obj;
        return Objects.equals(schedaTecnicaId, that.schedaTecnicaId) &&
                Objects.equals(analisiId, that.analisiId);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("{");
        result.append("schedaTecnicaId: " + schedaTecnicaId);
        result.append(", analisiId: " + analisiId);
        result.append("}");

        return result.toString();
    }
}

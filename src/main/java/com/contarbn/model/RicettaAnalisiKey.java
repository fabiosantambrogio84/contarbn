package com.contarbn.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Data
@Embeddable
public class RicettaAnalisiKey implements Serializable {

    private static final long serialVersionUID = 6332024596374972468L;

    @Column(name = "id_ricetta")
    Long ricettaId;

    @Column(name = "id_analisi")
    Long analisiId;

    public RicettaAnalisiKey(){}

    public RicettaAnalisiKey(Long ricettaId, Long analisiId){
        this.ricettaId = ricettaId;
        this.analisiId = analisiId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ricettaId, analisiId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final RicettaAnalisiKey that = (RicettaAnalisiKey) obj;
        return Objects.equals(ricettaId, that.ricettaId) &&
                Objects.equals(analisiId, that.analisiId);
    }

    @Override
    public String toString() {

        return "{" +
                "schedaTecnicaId: " + ricettaId +
                ", analisiId: " + analisiId +
                "}";
    }
}

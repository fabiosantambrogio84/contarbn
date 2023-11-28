package com.contarbn.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RicettaAllergeneKey implements Serializable {

    private static final long serialVersionUID = 7436766109057204106L;

    @Column(name = "id_ricetta")
    Long ricettaId;

    @Column(name = "id_allergene")
    Long allergeneId;

    public RicettaAllergeneKey(){}

    public RicettaAllergeneKey(Long ricettaId, Long allergeneId){
        this.ricettaId = ricettaId;
        this.allergeneId = allergeneId;
    }

    public Long getRicettaId() {
        return ricettaId;
    }

    public void setRicettaId(Long ricettaId) {
        this.ricettaId = ricettaId;
    }

    public Long getAllergeneId() {
        return allergeneId;
    }

    public void setAllergeneId(Long allergeneId) {
        this.allergeneId = allergeneId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ricettaId, allergeneId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final RicettaAllergeneKey that = (RicettaAllergeneKey) obj;
        return Objects.equals(ricettaId, that.ricettaId) &&
                Objects.equals(allergeneId, that.allergeneId);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("{");
        result.append("ricettaId: " + ricettaId);
        result.append(", allergeneId: " + allergeneId);
        result.append("}");

        return result.toString();
    }
}
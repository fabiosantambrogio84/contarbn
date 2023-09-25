package com.contarbn.model.stats;

import com.contarbn.model.DdtArticolo;
import com.contarbn.model.FatturaAccompagnatoriaArticolo;

import java.util.List;

public class ComputationObject {

    private List<DdtArticolo> ddtArticoli;

    private List<FatturaAccompagnatoriaArticolo> fattureAccompagnatorieArticoli;

    public List<DdtArticolo> getDdtArticoli() {
        return ddtArticoli;
    }

    public void setDdtArticoli(List<DdtArticolo> ddtArticoli) {
        this.ddtArticoli = ddtArticoli;
    }

    public List<FatturaAccompagnatoriaArticolo> getFattureAccompagnatorieArticoli() {
        return fattureAccompagnatorieArticoli;
    }

    public void setFattureAccompagnatorieArticoli(List<FatturaAccompagnatoriaArticolo> fattureAccompagnatorieArticoli) {
        this.fattureAccompagnatorieArticoli = fattureAccompagnatorieArticoli;
    }
}

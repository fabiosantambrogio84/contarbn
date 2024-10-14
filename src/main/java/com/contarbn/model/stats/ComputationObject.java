package com.contarbn.model.stats;

import com.contarbn.model.DdtArticolo;
import com.contarbn.model.FatturaAccompagnatoriaArticolo;
import com.contarbn.model.RicevutaPrivatoArticolo;
import lombok.Data;

import java.util.List;

@Data
public class ComputationObject {

    private List<DdtArticolo> ddtArticoli;

    private List<FatturaAccompagnatoriaArticolo> fattureAccompagnatorieArticoli;

    private List<RicevutaPrivatoArticolo> ricevutePrivatoArticoli;
}

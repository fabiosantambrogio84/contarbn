package com.contarbn.model.reports;

import com.contarbn.model.SchedaTecnicaNutriente;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SchedaTecnicaNutrienteDataSource {

    private String nutriente;

    private String valore;

    public static SchedaTecnicaNutrienteDataSource from(SchedaTecnicaNutriente schedaTecnicaNutriente){
        return SchedaTecnicaNutrienteDataSource.builder()
                .nutriente(schedaTecnicaNutriente.getNutriente().getNome())
                .valore(schedaTecnicaNutriente.getValore())
                .build();
    }
}
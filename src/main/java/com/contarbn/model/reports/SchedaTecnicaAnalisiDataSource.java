package com.contarbn.model.reports;

import com.contarbn.model.SchedaTecnicaAnalisi;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SchedaTecnicaAnalisiDataSource {

    private String analisi;

    private String risultato;

    public static SchedaTecnicaAnalisiDataSource from(SchedaTecnicaAnalisi schedaTecnicaAnalisi){
        return SchedaTecnicaAnalisiDataSource.builder()
                .analisi(schedaTecnicaAnalisi.getAnalisi().getNome())
                .risultato(schedaTecnicaAnalisi.getRisultato())
                .build();
    }
}
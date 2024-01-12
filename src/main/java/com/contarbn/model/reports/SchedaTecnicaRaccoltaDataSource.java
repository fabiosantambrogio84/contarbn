package com.contarbn.model.reports;

import com.contarbn.model.SchedaTecnicaRaccolta;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SchedaTecnicaRaccoltaDataSource {

    private String materiale;

    private String raccolta;

    public static SchedaTecnicaRaccoltaDataSource from(SchedaTecnicaRaccolta schedaTecnicaRaccolta){
        return SchedaTecnicaRaccoltaDataSource.builder()
                .materiale(schedaTecnicaRaccolta.getMateriale().getNome())
                .raccolta(schedaTecnicaRaccolta.getRaccolta().getNome())
                .build();
    }
}
package com.contarbn.model.reports;

import com.contarbn.model.SchedaTecnicaAnalisi;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Builder
public class SchedaTecnicaAnalisiDataSource {

    private String analisi;

    private String risultato;

    private Integer ordine;

    public static SchedaTecnicaAnalisiDataSource from(SchedaTecnicaAnalisi schedaTecnicaAnalisi){
        return SchedaTecnicaAnalisiDataSource.builder()
                .analisi(schedaTecnicaAnalisi.getAnalisi().getNome())
                .risultato(parseRisultato(schedaTecnicaAnalisi.getRisultato()))
                .ordine(schedaTecnicaAnalisi.getAnalisi().getOrdine())
                .build();
    }

    public static String parseRisultato(String risultato){
        if(StringUtils.isEmpty(risultato)){
            return risultato;
        }
        if(!risultato.contains("^")){
            return risultato;
        }
        String parsedRisultato = risultato;

        String regex = "(\\^\\s*([^\\s]+)\\s*)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(risultato);

        while (matcher.find()) {
            String substring = matcher.group(1);
            String replacement = substring.replace("^", "<sup>").replaceAll(" ","")+"</sup> ";
            parsedRisultato = parsedRisultato.replace(substring, replacement);
        }
        return parsedRisultato;
    }
}
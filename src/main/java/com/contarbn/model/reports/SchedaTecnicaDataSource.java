package com.contarbn.model.reports;

import com.contarbn.model.SchedaTecnica;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;

@Data
@Builder
public class SchedaTecnicaDataSource {

    private Integer numRevisione;

    private String data;

    private String codiceProdotto;

    private String prodotto;

    private String prodotto2;

    private String pesoNettoConfezione;

    private String ingredienti;

    private String tracce;

    private String durata;

    private String conservazione;

    private String consigliConsumo;

    private String tipologiaConfezionamento;

    private String imballo;

    private String imballoDimensioni;

    private String revisione;

    private String prodottoHtml;

    public static SchedaTecnicaDataSource from(SchedaTecnica schedaTecnica){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        String prodottoHtml = "<b>"+schedaTecnica.getProdotto()+"</b>";
        if(StringUtils.isNotEmpty(schedaTecnica.getProdotto2())){
            prodottoHtml += "<br/>"+schedaTecnica.getProdotto2();
        }

        return SchedaTecnicaDataSource.builder()
                .numRevisione(schedaTecnica.getNumRevisione())
                .data(simpleDateFormat.format(schedaTecnica.getData()))
                .codiceProdotto(schedaTecnica.getCodiceProdotto())
                .prodotto(schedaTecnica.getProdotto())
                .prodotto2(schedaTecnica.getProdotto2())
                .pesoNettoConfezione(schedaTecnica.getPesoNettoConfezione())
                .ingredienti(schedaTecnica.getIngredienti())
                .tracce(schedaTecnica.getTracce())
                .durata(schedaTecnica.getDurata())
                .conservazione(schedaTecnica.getConservazione())
                .consigliConsumo(schedaTecnica.getConsigliConsumo())
                .tipologiaConfezionamento(schedaTecnica.getTipologiaConfezionamento().getNome())
                .imballo(schedaTecnica.getImballo().getNome())
                .imballoDimensioni(schedaTecnica.getImballoDimensioni())
                .revisione("n "+schedaTecnica.getNumRevisione()+" del "+simpleDateFormat.format(schedaTecnica.getData()))
                .prodottoHtml(prodottoHtml)
                .build();
    }
}
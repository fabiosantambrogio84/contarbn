package com.contarbn.model.reports;

import com.contarbn.model.SchedaTecnica;
import lombok.Builder;
import lombok.Data;

import java.text.SimpleDateFormat;

@Data
@Builder
public class SchedaTecnicaDataSource {

    private Integer numRevisione;

    private String data;

    private String codiceProdotto;

    private String pesoNettoConfezione;

    private String ingredienti;

    private String allergeniTracce;

    private String durata;

    private String conservazione;

    private String consigliConsumo;

    private String tipologiaConfezionamento;

    private String imballo;

    private String imballoDimensioni;

    private String revisione;

    public static SchedaTecnicaDataSource from(SchedaTecnica schedaTecnica){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        return SchedaTecnicaDataSource.builder()
                .numRevisione(schedaTecnica.getNumRevisione())
                .data(simpleDateFormat.format(schedaTecnica.getData()))
                .codiceProdotto(schedaTecnica.getCodiceProdotto())
                .pesoNettoConfezione(schedaTecnica.getPesoNettoConfezione())
                .ingredienti(schedaTecnica.getIngredienti())
                .allergeniTracce(schedaTecnica.getAllergeniTracce())
                .durata(schedaTecnica.getDurata())
                .conservazione(schedaTecnica.getConservazione())
                .consigliConsumo(schedaTecnica.getConsigliConsumo())
                .tipologiaConfezionamento(schedaTecnica.getTipologiaConfezionamento().getNome())
                .imballo(schedaTecnica.getImballo().getNome())
                .imballoDimensioni(schedaTecnica.getImballoDimensioni())
                .revisione("n "+schedaTecnica.getNumRevisione()+" del "+simpleDateFormat.format(schedaTecnica.getData()))
                .build();
    }
}
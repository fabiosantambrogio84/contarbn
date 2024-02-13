package com.contarbn.model.beans;

import com.contarbn.model.Anagrafica;
import com.contarbn.model.SchedaTecnicaAnalisi;
import com.contarbn.model.SchedaTecnicaNutriente;
import com.contarbn.model.SchedaTecnicaRaccolta;
import lombok.Builder;
import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Set;

@Data
@Builder
public class SchedaTecnicaResponse {

    private String idView;

    private Long id;

    private Long idProduzione;

    private Long idArticolo;

    private Long idRicetta;

    private Integer numRevisione;

    private Integer anno;

    private Date data;

    private String codiceProdotto;

    private String prodotto;

    private String prodotto2;

    private String pesoNettoConfezione;

    private String ingredienti;

    private String tracce;

    private String durata;

    private String conservazione;

    private String consigliConsumo;

    private Anagrafica tipologiaConfezionamento;

    private Anagrafica imballo;

    private String imballoDimensioni;

    private Timestamp dataInserimento;

    private Timestamp dataAggiornamento;

    private String objectType;

    private Set<SchedaTecnicaNutriente> schedaTecnicaNutrienti;

    private Set<SchedaTecnicaAnalisi> schedaTecnicaAnalisi;

    private Set<SchedaTecnicaRaccolta> schedaTecnicaRaccolte;

    @Override
    public String toString() {

        return "{" +
                "idView: " + idView +
                ", id: " + id +
                ", idProduzione: " + idProduzione +
                ", idArticolo: " + idArticolo +
                ", idRicetta: " + idRicetta +
                ", numRevisione: " + numRevisione +
                ", anno: " + anno +
                ", data: " + data +
                ", codiceProdotto: " + codiceProdotto +
                ", prodotto: " + prodotto +
                ", prodotto2: " + prodotto2 +
                ", pesoNettoConfezione: " + pesoNettoConfezione +
                ", ingredienti: " + ingredienti +
                ", tracce: " + tracce +
                ", durata: " + durata +
                ", conservazione: " + conservazione +
                ", consigliConsumo: " + consigliConsumo +
                ", tipologiaConfezionamento: " + tipologiaConfezionamento +
                ", imballo: " + imballo +
                ", imballoDimensioni: " + imballoDimensioni +
                ", dataInserimento: " + dataInserimento +
                ", dataAggiornamento: " + dataAggiornamento +
                ", objectType: " + objectType +
                "}";
    }
}
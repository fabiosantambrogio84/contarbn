package com.contarbn.model.views;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;

@Data
@EqualsAndHashCode
@Entity
@Table(name = "v_scheda_tecnica_light")
public class VSchedaTecnicaLight {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "id_produzione")
    private Long idProduzione;

    @Column(name = "id_articolo")
    private Long idArticolo;

    @Column(name = "codice_prodotto")
    private String codiceProdotto;

    @Column(name = "prodotto")
    private String prodotto;

    @Column(name = "prodotto_descr")
    private String prodottoDescr;

    @Column(name = "num_revisione")
    private Integer numRevisione;

    @Column(name = "data")
    private Date data;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", idProduzione: " + idProduzione +
                ", idArticolo: " + idArticolo +
                ", codiceProdotto: " + codiceProdotto +
                ", prodotto: " + prodotto +
                ", prodottoDescr: " + prodottoDescr +
                ", numRevisione: " + numRevisione +
                ", data: " + data +
                "}";
    }
}
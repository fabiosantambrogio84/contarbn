package com.contarbn.model.views;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode()
@Entity
@Table(name = "v_giacenza_articolo_agg")
public class VGiacenzaArticolo {

    @Id
    @Column(name = "id_articolo")
    private Long idArticolo;

    @Column(name = "articolo")
    private String articolo;

    @Column(name = "prezzo_acquisto")
    private BigDecimal prezzoAcquisto;

    @Column(name = "prezzo_listino_base")
    private BigDecimal prezzoListinoBase;

    @Column(name = "quantita_tot")
    private Float quantita;

    @Column(name = "quantita_kg")
    private Float quantitaKg;

    @Column(name = "attivo")
    private Boolean attivo;

    @Column(name = "id_fornitore")
    private Long idFornitore;

    @Column(name = "fornitore")
    private String fornitore;

    @Column(name = "scaduto")
    private Integer scaduto;

    @Override
    public String toString() {

        return "{" +
                "idArticolo: " + idArticolo +
                ", articolo: " + articolo +
                ", prezzoAcquisto: " + prezzoAcquisto +
                ", prezzoListinoBase: " + prezzoListinoBase +
                ", quantita: " + quantita +
                ", quantitaKg: " + quantitaKg +
                ", attivo: " + attivo +
                ", idFornitore: " + idFornitore +
                ", fornitore: " + fornitore +
                ", scaduto: " + scaduto +
                "}";
    }
}

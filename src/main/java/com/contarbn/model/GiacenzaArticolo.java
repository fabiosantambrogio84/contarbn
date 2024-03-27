package com.contarbn.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Data
@EqualsAndHashCode
@Entity
@Table(name = "giacenza_articolo")
public class GiacenzaArticolo {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="id_articolo")
    private Articolo articolo;

    @Column(name = "lotto")
    private String lotto;

    @Column(name = "scadenza")
    private Date scadenza;

    @Column(name = "pezzi")
    private Integer pezzi;

    @Column(name = "quantita")
    private Float quantita;

    @Column(name = "data_inserimento")
    private Timestamp dataInserimento;

    @Column(name = "data_aggiornamento")
    private Timestamp dataAggiornamento;

    @Transient
    List<Movimentazione> movimentazioni;

    @Transient
    Integer scaduto;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", articolo: " + articolo +
                ", lotto: " + lotto +
                ", scadenza: " + scadenza +
                ", quantita: " + quantita +
                ", dataInserimento: " + dataInserimento +
                ", dataAggiornamento: " + dataAggiornamento +
                "}";

    }
}

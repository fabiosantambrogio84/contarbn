package com.contarbn.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@EqualsAndHashCode
@Data
@Entity
@Table(name = "movimentazione_manuale_articolo")
public class MovimentazioneManualeArticolo {

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

    @Column(name = "operation")
    private String operation;

    @Column(name = "context")
    private String context;

    @Column(name = "compute")
    private Boolean compute;

    @Column(name = "id_documento")
    private Long idDocumento;

    @Column(name = "num_documento")
    private String numDocumento;

    @Column(name = "anno_documento")
    private Integer annoDocumento;

    @Column(name = "fornitore_documento")
    private String fornitoreDocumento;

    @Column(name = "data_inserimento")
    private Timestamp dataInserimento;

    @Column(name = "data_aggiornamento")
    private Timestamp dataAggiornamento;

    @Transient
    List<Movimentazione> movimentazioni;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", articolo: " + articolo +
                ", lotto: " + lotto +
                ", scadenza: " + scadenza +
                ", pezzi: " + pezzi +
                ", quantita: " + quantita +
                ", operation: " + operation +
                ", context: " + context +
                ", idDocumento: " + idDocumento +
                ", numDocumento: " + numDocumento +
                ", annoDocumento: " + annoDocumento +
                ", fornitoreDocumento: " + fornitoreDocumento +
                ", dataInserimento: " + dataInserimento +
                ", dataAggiornamento: " + dataAggiornamento +
                "}";

    }
}

package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"notaResoTotali", "notaResoRighe", "notaResoPagamenti"})
@Entity
@Table(name = "nota_reso")
public class NotaReso {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "progressivo")
    private Integer progressivo;

    @Column(name = "anno")
    private Integer anno;

    @Column(name = "data")
    private Date data;

    @ManyToOne
    @JoinColumn(name="id_fornitore")
    private Fornitore fornitore;

    @ManyToOne
    @JoinColumn(name="id_stato")
    private StatoNotaReso statoNotaReso;

    @ManyToOne
    @JoinColumn(name="id_causale")
    private Causale causale;

    @Column(name = "spedito_ade")
    private Boolean speditoAde;

    @Column(name = "data_trasporto")
    private Date dataTrasporto;

    @Column(name = "ora_trasporto")
    private Time oraTrasporto;

    @ManyToOne
    @JoinColumn(name="id_trasportatore")
    private Trasportatore trasportatore;

    @Column(name = "totale")
    private BigDecimal totale;

    @Column(name = "totale_acconto")
    private BigDecimal totaleAcconto;

    @Column(name = "totale_quantita")
    private BigDecimal totaleQuantita;

    @Column(name = "note")
    private String note;

    @Column(name = "consegnato")
    private Boolean consegnato;

    @Column(name = "data_inserimento")
    private Timestamp dataInserimento;

    @Column(name = "data_aggiornamento")
    private Timestamp dataAggiornamento;

    @OneToMany(mappedBy = "notaReso")
    @JsonIgnoreProperties("notaReso")
    private Set<NotaResoTotale> notaResoTotali = new HashSet<>();

    @OneToMany(mappedBy = "notaReso")
    @JsonIgnoreProperties("notaReso")
    private Set<NotaResoRiga> notaResoRighe = new HashSet<>();

    @OneToMany(mappedBy = "notaReso")
    @JsonIgnoreProperties("notaReso")
    private Set<Pagamento> notaResoPagamenti = new HashSet<>();

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", progressivo: " + progressivo +
                ", anno: " + anno +
                ", data: " + data +
                ", fornitore: " + fornitore +
                ", stato: " + statoNotaReso +
                ", causale: " + causale +
                ", speditoAde: " + speditoAde +
                ", dataTrasporto: " + dataTrasporto +
                ", oraTrasporto: " + oraTrasporto +
                ", trasportatore: " + trasportatore +
                ", totale: " + totale +
                ", totaleAcconto: " + totaleAcconto +
                ", totaleQuantita: " + totaleQuantita +
                ", note: " + note +
                ", consegnato: " + consegnato +
                ", dataInserimento: " + dataInserimento +
                ", dataAggiornamento: " + dataAggiornamento +
                "}";
    }
}

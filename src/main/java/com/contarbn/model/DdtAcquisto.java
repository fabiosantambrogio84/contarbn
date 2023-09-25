package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"ddtAcquistoArticoli", "ddtAcquistoIngredienti", "ddtAcquistoPagamenti", "fatturaAcquistoDdtAcquisti"})
@Entity
@Table(name = "ddt_acquisto")
public class DdtAcquisto {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero")
    private String numero;

    @Column(name = "data")
    private Date data;

    @ManyToOne
    @JoinColumn(name="id_fornitore")
    private Fornitore fornitore;

    @ManyToOne
    @JoinColumn(name="id_stato")
    private StatoDdt statoDdt;

    @Column(name = "numero_colli")
    private Integer numeroColli;

    @Column(name = "totale_imponibile")
    private BigDecimal totaleImponibile;

    @Column(name = "totale_iva")
    private BigDecimal totaleIva;

    @Column(name = "totale")
    private BigDecimal totale;

    @Column(name = "totale_acconto")
    private BigDecimal totaleAcconto;

    @Column(name = "fatturato")
    private Boolean fatturato;

    @Column(name = "note")
    private String note;

    @Transient
    private Boolean modificaGiacenze;

    @Column(name = "data_inserimento")
    private Timestamp dataInserimento;

    @Column(name = "data_aggiornamento")
    private Timestamp dataAggiornamento;

    @OneToMany(mappedBy = "ddtAcquisto")
    @JsonIgnoreProperties("ddtAcquisto")
    private Set<DdtAcquistoArticolo> ddtAcquistoArticoli = new HashSet<>();

    @OneToMany(mappedBy = "ddtAcquisto")
    @JsonIgnoreProperties("ddtAcquisto")
    private Set<DdtAcquistoIngrediente> ddtAcquistoIngredienti = new HashSet<>();

    @OneToMany(mappedBy = "ddtAcquisto")
    @JsonIgnoreProperties("ddtAcquisto")
    private Set<Pagamento> ddtAcquistoPagamenti = new HashSet<>();

    @OneToMany(mappedBy = "ddtAcquisto")
    @JsonIgnore
    private Set<FatturaAcquistoDdtAcquisto> fatturaAcquistoDdtAcquisti = new HashSet<>();

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", numero: " + numero +
                ", data: " + data +
                ", fornitore: " + fornitore +
                ", stato: " + statoDdt +
                ", numeroColli: " + numeroColli +
                ", totaleImponibile: " + totaleImponibile +
                ", totaleIva: " + totaleIva +
                ", totale: " + totale +
                ", totaleAcconto: " + totaleAcconto +
                ", fatturato: " + fatturato +
                ", note: " + note +
                ", modificaGiacenze: " + modificaGiacenze +
                ", dataInserimento: " + dataInserimento +
                ", dataAggiornamento: " + dataAggiornamento +
                "}";
    }
}

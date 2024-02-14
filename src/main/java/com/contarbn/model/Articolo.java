package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"articoloImmagini", "sconti", "listiniPrezzi", "listiniPrezziVariazioni", "ordineClienteArticoli", "ddtArticoli", "ddtAcquistoArticoli", "fatturaAccompagnatoriaArticoli", "notaAccreditoRighe", "giacenze", "produzioni"})
@Entity
@Table(name = "articolo")
public class Articolo {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codice")
    private String codice;

    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "descrizione_2")
    private String descrizione2;

    @ManyToOne
    @JoinColumn(name="id_categoria")
    private CategoriaArticolo categoria;

    @ManyToOne
    @JoinColumn(name="id_fornitore")
    private Fornitore fornitore;

    @ManyToOne
    @JoinColumn(name="id_aliquota_iva")
    private AliquotaIva aliquotaIva;

    @ManyToOne
    @JoinColumn(name="id_unita_misura")
    private UnitaMisura unitaMisura;

    @Column(name = "data")
    private Date data;

    @Column(name = "quantita_predefinita")
    private Float quantitaPredefinita;

    @Column(name = "prezzo_acquisto")
    private BigDecimal prezzoAcquisto;

    @Column(name = "prezzo_listino_base")
    private BigDecimal prezzoListinoBase;

    @Column(name = "scadenza_giorni")
    private Integer scadenzaGiorni;

    @Column(name = "scadenza_giorni_allarme")
    private Integer scadenzaGiorniAllarme;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "complete_barcode")
    private Boolean completeBarcode;

    @Column(name = "barcode_mask_lotto_scadenza")
    private String barcodeMaskLottoScadenza;

    @Column(name = "barcode_regexp_lotto")
    private String barcodeRegexpLotto;

    @Column(name = "barcode_regexp_data_scadenza")
    private String barcodeRegexpDataScadenza;

    @Column(name = "sito_web")
    private Boolean sitoWeb;

    @Column(name = "attivo")
    private Boolean attivo;

    @Column(name = "data_inserimento")
    private Timestamp dataInserimento;

    @Column(name = "data_aggiornamento")
    private Timestamp dataAggiornamento;

    @OneToMany(mappedBy = "articolo")
    @JsonIgnore
    private List<ArticoloImmagine> articoloImmagini;

    @OneToMany(mappedBy = "articolo")
    @JsonIgnore
    private List<Sconto> sconti;

    @OneToMany(mappedBy = "articolo")
    @JsonIgnore
    private List<ListinoPrezzo> listiniPrezzi;

    @OneToMany(mappedBy = "articolo")
    @JsonIgnore
    private List<ListinoPrezzoVariazione> listiniPrezziVariazioni;

    @OneToMany(mappedBy = "articolo")
    @JsonIgnore
    private Set<OrdineClienteArticolo> ordineClienteArticoli = new HashSet<>();

    @OneToMany(mappedBy = "articolo")
    @JsonIgnore
    private Set<DdtArticolo> ddtArticoli = new HashSet<>();

    @OneToMany(mappedBy = "articolo")
    @JsonIgnore
    private Set<DdtAcquistoArticolo> ddtAcquistoArticoli = new HashSet<>();

    @OneToMany(mappedBy = "articolo")
    @JsonIgnore
    private Set<FatturaAccompagnatoriaArticolo> fatturaAccompagnatoriaArticoli = new HashSet<>();

    @OneToMany(mappedBy = "articolo")
    @JsonIgnore
    private Set<NotaAccreditoRiga> notaAccreditoRighe = new HashSet<>();

    @OneToMany(mappedBy = "articolo")
    @JsonIgnore
    private Set<GiacenzaArticolo> giacenze = new HashSet<>();

    @OneToMany(mappedBy = "articolo")
    @JsonIgnore
    private List<Produzione> produzioni;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", codice: " + codice +
                ", descrizione: " + descrizione +
                ", descrizione2: " + descrizione2 +
                ", categoria: " + categoria +
                ", fornitore: " + fornitore +
                ", aliquotaIva: " + aliquotaIva +
                ", unitaMisura: " + unitaMisura +
                ", data: " + data +
                ", quantitaPredefinita: " + quantitaPredefinita +
                ", prezzoAcquisto: " + prezzoAcquisto +
                ", prezzoListinoBase: " + prezzoListinoBase +
                ", scadenzaGiorni: " + scadenzaGiorni +
                ", scadenzaGiorniAllarme: " + scadenzaGiorniAllarme +
                ", barcode: " + barcode +
                ", completeBarcode: " + completeBarcode +
                ", barcodeMaskLottoScadenza: " + barcodeMaskLottoScadenza +
                ", barcodeRegexpLotto: " + barcodeRegexpLotto +
                ", barcodeRegexpDataScadenza: " + barcodeRegexpDataScadenza +
                ", sitoWeb: " + sitoWeb +
                ", attivo: " + attivo +
                ", dataInserimento: " + dataInserimento +
                ", dataAggiornamento: " + dataAggiornamento +
                "}";
    }
}

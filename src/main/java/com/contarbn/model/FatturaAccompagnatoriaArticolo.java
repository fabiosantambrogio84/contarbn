package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(exclude = {"fatturaAccompagnatoriaArticoliOrdiniClienti"})
@Entity
@Table(name = "fattura_accom_articolo")
public class FatturaAccompagnatoriaArticolo implements Serializable {

    private static final long serialVersionUID = 2645984869682222270L;

    @EmbeddedId
    FatturaAccompagnatoriaArticoloKey id;

    @ManyToOne
    @MapsId("id_fattura_accom")
    @JoinColumn(name = "id_fattura_accom")
    @JsonIgnoreProperties("fatturaAccompagnatoriaArticoli")
    private FatturaAccompagnatoria fatturaAccompagnatoria;

    @ManyToOne
    @MapsId("id_articolo")
    @JoinColumn(name = "id_articolo")
    @JsonIgnoreProperties("fatturaAccompagnatoriaArticoli")
    private Articolo articolo;

    @Column(name = "lotto")
    private String lotto;

    @Column(name = "scadenza")
    private Date scadenza;

    @Column(name = "quantita")
    private Float quantita;

    @Column(name = "numero_pezzi")
    private Integer numeroPezzi;

    @Column(name = "numero_pezzi_da_evadere")
    private Integer numeroPezziDaEvadere;

    @Column(name = "prezzo")
    private BigDecimal prezzo;

    @Column(name = "sconto")
    private BigDecimal sconto;

    @Column(name = "imponibile")
    private BigDecimal imponibile;

    @Column(name = "costo")
    private BigDecimal costo;

    @Column(name = "totale")
    private BigDecimal totale;

    @Column(name = "data_inserimento")
    private Timestamp dataInserimento;

    @Column(name = "data_aggiornamento")
    private Timestamp dataAggiornamento;

    @Transient
    private List<Long> idOrdiniClienti;

    @OneToMany(mappedBy = "fatturaAccompagnatoriaArticolo")
    @JsonIgnoreProperties("fatturaAccompagnatoriaArticolo")
    private Set<FatturaAccompagnatoriaArticoloOrdineCliente> fatturaAccompagnatoriaArticoliOrdiniClienti = new HashSet<>();

    public FatturaAccompagnatoriaArticoloKey getId() {
        return id;
    }

    public void setId(FatturaAccompagnatoriaArticoloKey id) {
        this.id = id;
    }

    public FatturaAccompagnatoria getFatturaAccompagnatoria() {
        return fatturaAccompagnatoria;
    }

    public void setFatturaAccompagnatoria(FatturaAccompagnatoria fatturaAccompagnatoria) {
        this.fatturaAccompagnatoria = fatturaAccompagnatoria;
    }

    public Articolo getArticolo() {
        return articolo;
    }

    public void setArticolo(Articolo articolo) {
        this.articolo = articolo;
    }

    public String getLotto() {
        return lotto;
    }

    public void setLotto(String lotto) {
        this.lotto = lotto;
    }

    public Date getScadenza() {
        return scadenza;
    }

    public void setScadenza(Date scadenza) {
        this.scadenza = scadenza;
    }

    public Float getQuantita() {
        return quantita;
    }

    public void setQuantita(Float quantita) {
        this.quantita = quantita;
    }

    public Integer getNumeroPezzi() {
        return numeroPezzi;
    }

    public void setNumeroPezzi(Integer numeroPezzi) {
        this.numeroPezzi = numeroPezzi;
    }

    public Integer getNumeroPezziDaEvadere() {
        return numeroPezziDaEvadere;
    }

    public void setNumeroPezziDaEvadere(Integer numeroPezziDaEvadere) {
        this.numeroPezziDaEvadere = numeroPezziDaEvadere;
    }

    public BigDecimal getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(BigDecimal prezzo) {
        this.prezzo = prezzo;
    }

    public BigDecimal getSconto() {
        return sconto;
    }

    public void setSconto(BigDecimal sconto) {
        this.sconto = sconto;
    }

    public BigDecimal getImponibile() {
        return imponibile;
    }

    public void setImponibile(BigDecimal imponibile) {
        this.imponibile = imponibile;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public BigDecimal getTotale() {
        return totale;
    }

    public void setTotale(BigDecimal totale) {
        this.totale = totale;
    }

    public Timestamp getDataInserimento() {
        return dataInserimento;
    }

    public void setDataInserimento(Timestamp dataInserimento) {
        this.dataInserimento = dataInserimento;
    }

    public Timestamp getDataAggiornamento() {
        return dataAggiornamento;
    }

    public void setDataAggiornamento(Timestamp dataAggiornamento) {
        this.dataAggiornamento = dataAggiornamento;
    }

    public List<Long> getIdOrdiniClienti() {
        return idOrdiniClienti;
    }

    public void setIdOrdiniClienti(List<Long> idOrdiniClienti) {
        this.idOrdiniClienti = idOrdiniClienti;
    }

    public Set<FatturaAccompagnatoriaArticoloOrdineCliente> getFatturaAccompagnatoriaArticoliOrdiniClienti() {
        return fatturaAccompagnatoriaArticoliOrdiniClienti;
    }

    public void setFatturaAccompagnatoriaArticoliOrdiniClienti(Set<FatturaAccompagnatoriaArticoloOrdineCliente> fatturaAccompagnatoriaArticoliOrdiniClienti) {
        this.fatturaAccompagnatoriaArticoliOrdiniClienti = fatturaAccompagnatoriaArticoliOrdiniClienti;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("{");
        result.append("fatturaAccompagnatoriaId: " + id.fatturaAccompagnatoriaId);
        result.append(", articoloId: " + id.articoloId);
        result.append(", lotto: " + lotto);
        result.append(", scadenza: " + scadenza);
        result.append(", quantita: " + quantita);
        result.append(", numeroPezzi: " + numeroPezzi);
        result.append(", numeroPezziDaEvadere: " + numeroPezziDaEvadere);
        result.append(", prezzo: " + prezzo);
        result.append(", sconto: " + sconto);
        result.append(", imponibile: " + imponibile);
        result.append(", costo: " + costo);
        result.append(", totale: " + totale);
        result.append(", dataInserimento: " + dataInserimento);
        result.append(", dataAggiornamento: " + dataAggiornamento);
        result.append(", idOrdiniClienti: " + idOrdiniClienti);
        result.append("}");

        return result.toString();
    }
}

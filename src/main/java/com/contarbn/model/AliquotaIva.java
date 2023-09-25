package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(exclude = {"articoli", "fatturaAccompagnatoriaTotali", "notaAccreditoTotali", "notaAccreditoRighe", "notaResoTotali", "notaResoRighe"})
@Entity
@Table(name = "aliquota_iva")
public class AliquotaIva {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "valore")
    private BigDecimal valore;

    @Column(name = "data_inserimento")
    private Timestamp dataInserimento;

    @OneToMany(mappedBy = "aliquotaIva")
    @JsonIgnore
    private List<Articolo> articoli;

    @OneToMany(mappedBy = "aliquotaIva")
    @JsonIgnore
    private Set<FatturaAccompagnatoriaTotale> fatturaAccompagnatoriaTotali = new HashSet<>();

    @OneToMany(mappedBy = "aliquotaIva")
    @JsonIgnore
    private Set<NotaAccreditoTotale> notaAccreditoTotali = new HashSet<>();

    @OneToMany(mappedBy = "aliquotaIva")
    @JsonIgnore
    private Set<NotaAccreditoRiga> notaAccreditoRighe = new HashSet<>();

    @OneToMany(mappedBy = "aliquotaIva")
    @JsonIgnore
    private Set<NotaResoTotale> notaResoTotali = new HashSet<>();

    @OneToMany(mappedBy = "aliquotaIva")
    @JsonIgnore
    private Set<NotaResoRiga> notaResoRighe = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getValore() {
        return valore;
    }

    public void setValore(BigDecimal valore) {
        this.valore = valore;
    }

    public Timestamp getDataInserimento() {
        return dataInserimento;
    }

    public void setDataInserimento(Timestamp dataInserimento) {
        this.dataInserimento = dataInserimento;
    }

    public List<Articolo> getArticoli() {
        return articoli;
    }

    public void setArticoli(List<Articolo> articoli) {
        this.articoli = articoli;
    }

    public Set<FatturaAccompagnatoriaTotale> getFatturaAccompagnatoriaTotali() {
        return fatturaAccompagnatoriaTotali;
    }

    public void setFatturaAccompagnatoriaTotali(Set<FatturaAccompagnatoriaTotale> fatturaAccompagnatoriaTotali) {
        this.fatturaAccompagnatoriaTotali = fatturaAccompagnatoriaTotali;
    }

    public Set<NotaAccreditoTotale> getNotaAccreditoTotali() {
        return notaAccreditoTotali;
    }

    public void setNotaAccreditoTotali(Set<NotaAccreditoTotale> notaAccreditoTotali) {
        this.notaAccreditoTotali = notaAccreditoTotali;
    }

    public Set<NotaAccreditoRiga> getNotaAccreditoRighe() {
        return notaAccreditoRighe;
    }

    public void setNotaAccreditoRighe(Set<NotaAccreditoRiga> notaAccreditoRighe) {
        this.notaAccreditoRighe = notaAccreditoRighe;
    }

    public Set<NotaResoTotale> getNotaResoTotali() {
        return notaResoTotali;
    }

    public void setNotaResoTotali(Set<NotaResoTotale> notaResoTotali) {
        this.notaResoTotali = notaResoTotali;
    }

    public Set<NotaResoRiga> getNotaResoRighe() {
        return notaResoRighe;
    }

    public void setNotaResoRighe(Set<NotaResoRiga> notaResoRighe) {
        this.notaResoRighe = notaResoRighe;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("id: " + id);
        result.append(", valore: " + valore);
        result.append(", dataInserimento: " + dataInserimento);
        result.append("}");

        return result.toString();

    }
}

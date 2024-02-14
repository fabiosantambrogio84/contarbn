package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(exclude = {"ricettaIngredienti", "ingredienteAllergeni", "ingredienteAllergeniComposizione"})
@Data
@Entity
@Table(name = "ingrediente")
public class Ingrediente {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codice")
    private String codice;

    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "prezzo")
    private BigDecimal prezzo;

    @ManyToOne
    @JoinColumn(name = "id_unita_misura")
    private UnitaMisura unitaMisura;

    @ManyToOne
    @JoinColumn(name="id_fornitore")
    private Fornitore fornitore;

    @ManyToOne
    @JoinColumn(name="id_aliquota_iva")
    private AliquotaIva aliquotaIva;

    @Column(name = "scadenza_giorni_allarme")
    private Integer scadenzaGiorniAllarme;

    @Column(name = "data_inserimento")
    private Timestamp dataInserimento;

    @Column(name = "composto")
    private Boolean composto;

    @Column(name = "composizione")
    private String composizione;

    @Column(name = "attivo")
    private Boolean attivo;

    @Column(name = "note")
    private String note;

    @OneToMany(mappedBy = "ingrediente")
    @JsonIgnore
    Set<RicettaIngrediente> ricettaIngredienti = new HashSet<>();

    @OneToMany(mappedBy = "ingrediente")
    @JsonIgnoreProperties("ingrediente")
    private Set<IngredienteAllergene> ingredienteAllergeni = new HashSet<>();

    @OneToMany(mappedBy = "ingrediente")
    @JsonIgnoreProperties("ingrediente")
    private Set<IngredienteAllergeneComposizione> ingredienteAllergeniComposizione = new HashSet<>();

    @Override
    public String toString() {
        return "{" +
                "id: " + id +
                ", codice: " + codice +
                ", descrizione: " + descrizione +
                ", prezzo: " + prezzo +
                ", unitaMisura: " + unitaMisura +
                ", fornitore: " + fornitore +
                ", fornitore: " + aliquotaIva +
                ", scadenzaGiorniAllarme: " + scadenzaGiorniAllarme +
                ", dataInserimento: " + dataInserimento +
                ", composto: " + composto +
                ", composizione: " + composizione +
                ", attivo: " + attivo +
                ", note: " + note +
                "}";

    }
}
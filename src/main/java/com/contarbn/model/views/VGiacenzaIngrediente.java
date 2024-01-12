package com.contarbn.model.views;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@EqualsAndHashCode
@Entity
@Table(name = "v_giacenza_ingrediente_agg")
public class VGiacenzaIngrediente {

    @Id
    @Column(name = "id_ingrediente")
    private Long idIngrediente;

    @Column(name = "ingrediente")
    private String ingrediente;

    @Column(name = "quantita_tot")
    private Float quantita;

    @Column(name = "attivo")
    private Boolean attivo;

    @Column(name = "udm")
    private String udm;

    @Column(name = "id_fornitore")
    private Long idFornitore;

    @Column(name = "fornitore")
    private String fornitore;

    @Column(name = "codice_ingrediente")
    private String codiceIngrediente;

    @Column(name = "descrizione_ingrediente")
    private String descrizioneIngrediente;

    @Column(name = "scaduto")
    private Integer scaduto;

    @Override
    public String toString() {

        return "{" +
                "idIngrediente: " + idIngrediente +
                ", ingrediente: " + ingrediente +
                ", quantita: " + quantita +
                ", attivo: " + attivo +
                ", udm: " + udm +
                ", idFornitore: " + idFornitore +
                ", fornitore: " + fornitore +
                ", codiceIngrediente: " + codiceIngrediente +
                ", descrizioneIngrediente: " + descrizioneIngrediente +
                ", scaduto: " + scaduto +
                "}";
    }
}

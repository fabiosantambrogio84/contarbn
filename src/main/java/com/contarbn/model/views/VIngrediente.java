package com.contarbn.model.views;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

@EqualsAndHashCode
@Data
@Entity
@Table(name = "v_ingrediente")
public class VIngrediente {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "codice")
    private String codice;

    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "prezzo")
    private BigDecimal prezzo;

    @Column(name = "id_unita_misura")
    private Long idUnitaMisura;

    @Column(name = "unita_misura")
    private String unitaMisura;

    @Column(name = "id_fornitore")
    private Long idFornitore;

    @Column(name = "fornitore")
    private String fornitore;

    @Column(name = "id_aliquota_iva")
    private Long idAliquotaIva;

    @Column(name = "aliquota_iva")
    private BigDecimal aliquotaIva;

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

    @Override
    public String toString() {
        return "{" +
                "id: " + id +
                ", codice: " + codice +
                ", descrizione: " + descrizione +
                ", prezzo: " + prezzo +
                ", idUnitaMisura: " + idUnitaMisura +
                ", unitaMisura: " + unitaMisura +
                ", idFornitore: " + idFornitore +
                ", fornitore: " + fornitore +
                ", idAliquotaIva: " + idAliquotaIva +
                ", aliquotaIva: " + aliquotaIva +
                ", scadenzaGiorniAllarme: " + scadenzaGiorniAllarme +
                ", dataInserimento: " + dataInserimento +
                ", composto: " + composto +
                ", composizione: " + composizione +
                ", attivo: " + attivo +
                ", note: " + note +
                "}";

    }
}
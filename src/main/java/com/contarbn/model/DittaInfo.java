package com.contarbn.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.sql.Timestamp;

@SuppressWarnings("ALL")
@EqualsAndHashCode
@Data
@Entity
@Table(name = "ditta_info")
public class DittaInfo {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codice")
    private String codice;

    @Column(name = "dato")
    private String dato;

    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "valore")
    private String valore;

    @Column(name = "deletable")
    private Boolean deletable;

    @Column(name = "data_inserimento")
    private Timestamp dataInserimento;

    @Column(name = "data_aggiornamento")
    private Timestamp dataAggiornamento;

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("{");
        result.append("id: " + id);
        result.append(", codice: " + codice);
        result.append(", dato: " + dato);
        result.append(", descrizione: " + descrizione);
        result.append(", valore: " + valore);
        result.append(", deletable: " + deletable);
        result.append(", dataInserimento: " + dataInserimento);
        result.append(", dataAggiornamento: " + dataAggiornamento);
        result.append("}");

        return result.toString();
    }
}

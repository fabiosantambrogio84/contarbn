package com.contarbn.model;

import com.contarbn.util.enumeration.TipologiaAnagrafica;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name = "anagrafica")
public class Anagrafica {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo")
    @Enumerated(EnumType.STRING)
    private TipologiaAnagrafica tipo;

    @Column(name = "nome")
    private String nome;

    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "ordine")
    private Integer ordine;

    @Column(name = "attivo")
    private Boolean attivo;

    @Column(name = "data_inserimento")
    private Timestamp dataInserimento;

    @Column(name = "data_aggiornamento")
    private Timestamp dataAggiornamento;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", tipo: " + tipo +
                ", nome: " + nome +
                ", descrizione: " + descrizione +
                ", ordine: " + ordine +
                ", attivo: " + attivo +
                ", dataInserimento: " + dataInserimento +
                ", dataAggiornamento: " + dataAggiornamento +
                "}";
    }
}
package com.contarbn.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.sql.Timestamp;

@EqualsAndHashCode
@Data
@Entity
@Table(name = "allergene")
public class Allergene {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String nome;

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
                ", nome: " + nome +
                ", ordine: " + ordine +
                ", attivo: " + attivo +
                ", dataInserimento: " + dataInserimento +
                ", dataAggiornamento: " + dataAggiornamento +
                "}";
    }
}

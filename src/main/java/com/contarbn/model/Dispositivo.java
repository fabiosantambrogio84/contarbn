package com.contarbn.model;

import com.contarbn.util.enumeration.TipologiaDispositivo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@EqualsAndHashCode
@Entity
@Table(name = "dispositivo")
public class Dispositivo {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo")
    @Enumerated(EnumType.STRING)
    private TipologiaDispositivo tipo;

    @Column(name = "nome")
    private String nome;

    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "ip")
    private String ip;

    @Column(name = "porta")
    private Integer porta;

    @Column(name = "predefinito")
    private Boolean predefinito;

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
                ", ip: " + ip +
                ", porta: " + porta +
                ", predefinito: " + predefinito +
                ", attivo: " + attivo +
                ", dataInserimento: " + dataInserimento +
                ", dataAggiornamento: " + dataAggiornamento +
                "}";
    }
}
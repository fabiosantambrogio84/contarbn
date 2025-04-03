package com.contarbn.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "bordero")
public class Bordero {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_autista")
    private Integer idAutista;

    @Column(name = "id_trasportatore")
    private Integer idTrasportatore;

    @Column(name = "data_consegna")
    private Date dataConsegna;

    @Column(name = "data_inserimento")
    private Timestamp dataInserimento;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", idAutista: " + idAutista +
                ", idTrasportatore: " + idTrasportatore +
                ", dataConsegna: " + dataConsegna +
                ", dataInserimento: " + dataInserimento +
                "}";
    }
}

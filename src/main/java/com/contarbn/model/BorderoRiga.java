package com.contarbn.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "bordero_riga")
public class BorderoRiga {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_bordero")
    private Integer idBordero;

    @Column(name = "progressivo")
    private Integer progressivo;

    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "id_fornitore")
    private Integer idFornitore;

    @Column(name = "id_punto_consegna")
    private Integer idPuntoConsegna;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "note")
    private String note;

    @Column(name = "firma")
    private String firma;

    @Column(name = "data_inserimento")
    private Timestamp dataInserimento;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", idBordero: " + idBordero +
                ", progressivo: " + progressivo +
                ", idCliente: " + idCliente +
                ", idPuntoConsegna: " + idPuntoConsegna +
                ", idFornitore: " + idFornitore +
                ", telefono: " + telefono +
                ", note: " + note +
                ", firma: " + firma +
                ", dataInserimento: " + dataInserimento +
                "}";
    }
}

package com.contarbn.model.views;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@EqualsAndHashCode()
@Entity
@Table(name = "v_bordero_riga")
public class VBorderoRiga {

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "id_bordero")
    private Integer idBordero;

    @Column(name = "progressivo")
    private Integer progressivo;

    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "id_fornitore")
    private Integer idFornitore;

    @Column(name = "cliente_fornitore")
    private String clienteFornitore;

    @Column(name = "id_punto_consegna")
    private Integer idPuntoConsegna;

    @Column(name = "punto_consegna")
    private String puntoConsegna;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "note")
    private String note;

    @Column(name = "firma")
    private String firma;

    @Override
    public String toString() {

        return "{" +
                "uuid: " + uuid +
                ", idBordero: " + idBordero +
                ", progressivo: " + progressivo +
                ", idCliente: " + idCliente +
                ", idFornitore: " + idFornitore +
                ", clienteFornitore: " + clienteFornitore +
                ", idPuntoConsegna: " + idPuntoConsegna +
                ", puntoConsegna: " + puntoConsegna +
                ", telefono: " + telefono +
                ", note: " + note +
                ", firma: " + firma +
                "}";
    }
}

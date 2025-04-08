package com.contarbn.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "bordero_detail")
public class BorderoDetail {

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "id_bordero")
    private Integer idBordero;

    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "id_punto_consegna")
    private Integer idPuntoConsegna;

    @Column(name = "id_fornitore")
    private Integer idFornitore;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "id_documento")
    private Integer idDocumento;

    @Column(name = "data_consegna")
    private Date dataConsegna;

    @Column(name = "note")
    private String note;

    @Column(name = "data_inserimento")
    private Timestamp dataInserimento;

    @Override
    public String toString() {

        return "{" +
                "uuid: " + uuid +
                ", idBordero: " + idBordero +
                ", idCliente: " + idCliente +
                ", idPuntoConsegna: " + idPuntoConsegna +
                ", idFornitore: " + idFornitore +
                ", tipoDocumento: " + tipoDocumento +
                ", idDocumento: " + idDocumento +
                ", dataConsegna: " + dataConsegna +
                ", note: " + note +
                ", dataInserimento: " + dataInserimento +
                "}";
    }
}

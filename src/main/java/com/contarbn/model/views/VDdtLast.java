package com.contarbn.model.views;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@EqualsAndHashCode()
@Entity
@Table(name = "v_ddt_last")
public class VDdtLast {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "id_autista")
    private Long idAutista;

    @Column(name = "id_trasportatore")
    private Long idTrasportatore;

    @Column(name = "tipo_trasporto")
    private String tipoTrasporto;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", idAutista: " + idAutista +
                ", idTrasportatore: " + idTrasportatore +
                ", tipoTrasporto: " + tipoTrasporto +
                "}";
    }
}

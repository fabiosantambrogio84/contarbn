package com.contarbn.model.views;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;

@Data
@EqualsAndHashCode()
@Entity
@Table(name = "v_documento_acquisto_ingrediente")
public class VDocumentoAcquistoIngrediente {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "id_documento")
    private Long idDocumento;

    @Column(name = "num_documento")
    private String numDocumento;

    @Column(name = "data_documento")
    private Date dataDocumento;

    @Column(name = "id_fornitore")
    private Long idFornitore;

    @Column(name = "ragione_sociale_fornitore")
    private String ragioneSocialeFornitore;

    @Column(name = "lotto_ingrediente")
    private String lottoIngrediente;


    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", tipoDocumento: " + tipoDocumento +
                ", idDocumento: " + idDocumento +
                ", numDocumento: " + numDocumento +
                ", dataDocumento: " + dataDocumento +
                ", idFornitore: " + idFornitore +
                ", ragioneSocialeFornitore: " + ragioneSocialeFornitore +
                ", lottoIngrediente: " + lottoIngrediente +
                "}";
    }
}

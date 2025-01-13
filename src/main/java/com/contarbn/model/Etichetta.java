package com.contarbn.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

@EqualsAndHashCode
@Data
@Entity
@Table(name = "etichetta")
public class Etichetta {

    @Id
    private String uuid;

    @Column(name = "articolo")
    private String articolo;

    @Column(name = "lotto")
    private String lotto;

    @Column(name = "peso")
    private BigDecimal peso;

    @Column(name = "filename")
    private String filename;

    @Column(name = "file_content")
    private byte[] fileContent;

    @Column(name = "id_dispositivo")
    private Long idDispositivo;

    @Column(name = "data_inserimento")
    private Timestamp dataInserimento;

    @Override
    public String toString() {

        return "{" +
                "uuid: " + uuid +
                ", articolo: " + articolo +
                ", lotto: " + lotto +
                ", peso: " + peso +
                ", filename: " + filename +
                ", idDispositivo: " + idDispositivo +
                ", dataInserimento: " + dataInserimento +
                "}";
    }
}

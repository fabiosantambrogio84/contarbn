package com.contarbn.model.views;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Date;

@Data
@EqualsAndHashCode()
@Entity
@Table(name = "v_ddt")
public class VDdt {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "anno_contabile")
    private Integer annoContabile;

    @Column(name = "progressivo")
    private Integer progressivo;

    @Column(name = "data")
    private Date data;

    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(name = "cliente")
    private String cliente;

    @Column(name = "cliente_email")
    private String clienteEmail;

    @Column(name = "id_agente")
    private Long idAgente;

    @Column(name = "agente")
    private String agente;

    @Column(name = "id_autista")
    private Long idAutista;

    @Column(name = "id_stato")
    private Long idStato;

    @Column(name = "stato")
    private String stato;

    @Column(name = "fatturato")
    private Boolean fatturato;

    @Column(name = "totale_acconto")
    private BigDecimal totaleAcconto;

    @Column(name = "totale")
    private BigDecimal totale;

    @Column(name = "totale_imponibile")
    private BigDecimal totaleImponibile;

    @Column(name = "totale_costo")
    private BigDecimal totaleCosto;

    @Column(name = "totale_iva")
    private BigDecimal totaleIva;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", annoContabile: " + annoContabile +
                ", progressivo: " + progressivo +
                ", data: " + data +
                ", idCliente: " + idCliente +
                ", cliente: " + cliente +
                ", clienteEmail: " + clienteEmail +
                ", idAgente: " + idAgente +
                ", agente: " + agente +
                ", idAutista: " + idAutista +
                ", idStato: " + idStato +
                ", stato: " + stato +
                ", fatturato: " + fatturato +
                ", totaleAcconto: " + totaleAcconto +
                ", totale: " + totale +
                ", totaleImponibile: " + totaleImponibile +
                ", totaleCosto: " + totaleCosto +
                ", totaleIva: " + totaleIva +
                "}";
    }
}

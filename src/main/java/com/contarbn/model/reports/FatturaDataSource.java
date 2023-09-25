package com.contarbn.model.reports;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FatturaDataSource {

    private String numero;

    private String data;

    private String clientePartitaIva;

    private String clienteCodiceFiscale;

    private String clienteDescrizione;

    private String causale;

    private String pagamento;

    private String agente;

    private BigDecimal acconto;

    private BigDecimal totale;

    private BigDecimal totaleDaPagare;
}
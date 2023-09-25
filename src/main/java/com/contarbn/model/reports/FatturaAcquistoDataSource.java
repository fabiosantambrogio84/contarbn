package com.contarbn.model.reports;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FatturaAcquistoDataSource {

    private String numero;

    private String data;

    private String fornitorePartitaIva;

    private String fornitoreCodiceFiscale;

    private String fornitoreDescrizione;

    private String causale;

    private String pagamento;

    private String agente;

    private BigDecimal acconto;

    private BigDecimal totale;

    private BigDecimal totaleDaPagare;
}
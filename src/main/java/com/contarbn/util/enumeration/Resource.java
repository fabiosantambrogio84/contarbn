package com.contarbn.util.enumeration;

public enum Resource {

    BANCA("banca"),
    CAUSALE("causale"),
    CLIENTE("cliente"),
    DDT("ddt"),
    DDT_ACQUISTO("ddt acquisto"),
    FATTURA("fattura"),
    FATTURA_ACQUISTO("fattura acquisto"),
    FATTURA_ACCOMPAGNATORIA("fattura accompagnatoria"),
    FATTURA_ACCOMPAGNATORIA_ACQUISTO("fattura accompagnatoria acquisto"),
    FORNITORE("fornitore"),
    MOVIMENTAZIONE_MANUALE_ARTICOLO("movimentazione manuale articolo"),
    MOVIMENTAZIONE_MANUALE_INGREDIENTE("movimentazione manuale ingrediente"),
    NOTA_ACCREDITO("nota accredito"),
    NOTA_RESO("nota reso"),
    ORDINE_CLIENTE("ordine cliente"),
    PRODUZIONE("produzione"),
    PRODUZIONE_SCORTA("produzione scorta"),
    PRODUZIONE_INGREDIENTE("produzione ingrediente"),
    RICEVUTA_PRIVATO("ricevuta privato");

    private final String label;

    Resource(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

package com.contarbn.util.enumeration;

public enum Documento {

    DDT("ddt", "vendita", 1),
    DDT_ACQUISTO("ddt-acquisto", "acquisto", 2),
    FATTURA_ACCOMPAGNATORIA("fattura-accompagnatoria", "vendita",3),
    FATTURA_ACCOMPAGNATORIA_ACQUISTO("fattura-accompagnatoria-acquisto", "acquisto", 4),
    RICEVUTA_PRIVATO("ricevuta-privato", "vendita",5);

    private final String name;

    private final String type;

    private final Integer order;

    Documento(String name, String type, Integer order) {
        this.name = name;
        this.type = type;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public Integer getOrder() {
        return order;
    }

}
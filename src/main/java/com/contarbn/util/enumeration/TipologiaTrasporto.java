package com.contarbn.util.enumeration;

public enum TipologiaTrasporto {

    MITTENTE("Mittente", 2, true),
    DESTINATARIO("Destinatario", 1, false),
    VETTORE("Vettore", 3, false);

    private final String label;

    private final Integer order;

    private Boolean predefinito;

    TipologiaTrasporto(String label, Integer order, Boolean predefinito) {
        this.label = label;
        this.order = order;
        this.predefinito = predefinito;
    }

    public String getLabel() {
        return label;
    }

    public Integer getOrder() {
        return order;
    }

    public Boolean getPredefinito(){return predefinito;}

}

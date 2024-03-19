package com.contarbn.util.enumeration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum TipologiaTrasportoDdt {

    MITTENTE("Mittente", 2, true),
    DESTINATARIO("Destinatario", 1, false),
    VETTORE("Vettore", 3, false);

    private final String label;

    private final Integer order;

    private Boolean predefinito;

    TipologiaTrasportoDdt(String label, Integer order, Boolean predefinito) {
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

    public void setPredefinito(Boolean predefinito){
        this.predefinito = predefinito;
    }

    public static List<String> labels(){
        return Arrays.stream(TipologiaTrasportoDdt.values()).map(TipologiaTrasportoDdt::getLabel).collect(Collectors.toList());
    }
}

package com.contarbn.util.enumeration;

import java.util.*;

public enum OperationAction {

    COMPUTE_GIACENZE_ARTICOLI("Ricalcola giacenze articoli"),
    DELETE_ORDINI_CLIENTI("Elimina ordini clienti evasi"),
    DELETE_ETICHETTE("Elimina etichette");

    private final String label;

    OperationAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static List<Map<String, String>> getActions(){
        List<Map<String, String>> actions = new ArrayList<>();
        Arrays.stream(OperationAction.values()).forEach(ca -> {
            Map<String, String> action = new HashMap<>();
            action.put("action", ca.name());
            action.put("label", ca.getLabel());
            actions.add(action);
        });

        return actions;
    }
}
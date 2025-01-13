package com.contarbn.util.enumeration;

public enum LabelPlaceholder {

    ARTICOLO("${articolo}"),
    INGREDIENTI("${ingredienti}"),
    TRACCE("${tracce}"),
    CONSERVAZIONE("${conservazione}"),
    VALORI_NUTRIZIONALI("${valoriNutrizionali}"),
    CONSUMAZIONE("01/01/1970"),
    LOTTO("${lotto}"),
    PESO("${peso}"),
    BARCODE_EAN_13("0000000000000"),
    BARCODE_EAN_128("123456789012");

    private final String placeholder;

    LabelPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return placeholder;
    }
}

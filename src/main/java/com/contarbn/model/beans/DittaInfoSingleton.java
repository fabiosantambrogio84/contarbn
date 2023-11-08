package com.contarbn.model.beans;

import com.contarbn.model.DittaInfo;

import java.util.HashMap;
import java.util.Map;

public class DittaInfoSingleton {

    private static DittaInfoSingleton INSTANCE;

    private final Map<String, DittaInfo> dittaInfoMap;

    private DittaInfoSingleton() {
        dittaInfoMap = new HashMap<>();
    }

    public static DittaInfoSingleton get() {
        if(INSTANCE == null) {
            INSTANCE = new DittaInfoSingleton();
        }
        return INSTANCE;
    }

    public void addDittaInfo(String codice, DittaInfo dittaInfo) {
        dittaInfoMap.put(codice, dittaInfo);
    }

    public Map<String, DittaInfo> getDittaInfoMap() {
        return dittaInfoMap;
    }
}

package com.contarbn.service;

import com.contarbn.model.views.VDocumentoLast;
import com.contarbn.repository.views.VDocumentoLastRepository;
import com.contarbn.util.enumeration.Documento;
import com.contarbn.util.enumeration.TipologiaTrasporto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Slf4j
@RequiredArgsConstructor
@Service
public class UtilsService {

    private final VDocumentoLastRepository vDocumentoLastRepository;

    public Map<String, Boolean> getTipologieTrasporto(String context){
        Map<String, Boolean> tipologieTrasportoMap = new HashMap<>();
        TipologiaTrasporto[] tipologieTrasporto = TipologiaTrasporto.values();

        String finalContext = StringUtils.isEmpty(context) ? "ddt" : context;

        Optional<VDocumentoLast> vDocumentoLast;
        switch (finalContext) {
            case "ddt":
                vDocumentoLast = vDocumentoLastRepository.find(Documento.DDT.getName());
                break;
            case "fattura-accompagnatoria":
                vDocumentoLast = vDocumentoLastRepository.find(Documento.FATTURA_ACCOMPAGNATORIA.getName());
                break;
            case "ricevuta-privato":
                vDocumentoLast = vDocumentoLastRepository.find(Documento.RICEVUTA_PRIVATO.getName());
                break;
            case "fattura-accompagnatoria-acquisto":
                vDocumentoLast = vDocumentoLastRepository.find(Documento.FATTURA_ACCOMPAGNATORIA_ACQUISTO.getName());
                break;
            default:
                vDocumentoLast = Optional.empty();
        }

        for(TipologiaTrasporto tipologiaTrasporto : tipologieTrasporto){
            Boolean predefinito = tipologiaTrasporto.getPredefinito();
            if (vDocumentoLast.isPresent()) {
                if(tipologiaTrasporto.getLabel().equals(vDocumentoLast.get().getTipoTrasporto())){
                    predefinito = Boolean.TRUE;
                } else {
                    predefinito = Boolean.FALSE;
                }
            }
            tipologieTrasportoMap.put(tipologiaTrasporto.getLabel(), predefinito);
        }

        return new TreeMap<>(tipologieTrasportoMap);
    }
}

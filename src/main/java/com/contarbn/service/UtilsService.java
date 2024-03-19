package com.contarbn.service;

import com.contarbn.model.views.VDdtLast;
import com.contarbn.repository.views.VDdtLastRepository;
import com.contarbn.util.enumeration.TipologiaTrasportoDdt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UtilsService {

    private final VDdtLastRepository vDdtLastRepository;

    public Map<String, Boolean> getTipologieTrasportoDdt(){
        Map<String, Boolean> tipologieTrasporto = new HashMap<>();
        TipologiaTrasportoDdt[] tipologieTrasportoDdt = TipologiaTrasportoDdt.values();
        Optional<VDdtLast> vDdtLast = vDdtLastRepository.find();
        for(TipologiaTrasportoDdt tipologiaTrasportoDdt : tipologieTrasportoDdt){
            Boolean predefinito = tipologiaTrasportoDdt.getPredefinito();
            if(vDdtLast.isPresent()){
                if(tipologiaTrasportoDdt.getLabel().equals(vDdtLast.get().getTipoTrasporto())){
                    predefinito = Boolean.TRUE;
                } else {
                    predefinito = Boolean.FALSE;
                }
            }
            tipologieTrasporto.put(tipologiaTrasportoDdt.getLabel(), predefinito);
        }

        return tipologieTrasporto;
    }
}

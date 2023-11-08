package com.contarbn.startup;

import com.contarbn.model.DittaInfo;
import com.contarbn.model.beans.DittaInfoSingleton;
import com.contarbn.service.DittaInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ApplicationEventListener {

    //private final CacheService cacheService;

    private final DittaInfoService dittaInfoService;

    public ApplicationEventListener(final DittaInfoService dittaInfoService){
        this.dittaInfoService = dittaInfoService;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        List<DittaInfo> dittaInfoList = dittaInfoService.getAll();
        if(!dittaInfoList.isEmpty()){
            for(DittaInfo dittaInfo : dittaInfoList){
                DittaInfoSingleton.get().addDittaInfo(dittaInfo.getCodice(), dittaInfo);
            }
        }

        /*
        log.info("Initializing cache...");
        Map<String, Integer> result = cacheService.initialize();
        if(!result.isEmpty()){
            for(String cacheName : result.keySet()){
                log.info("Cache {} initialized with {} entries", cacheName, result.get(cacheName));
            }
        } else {
            log.info("No caches has been initialized");
        }
        log.info("Successfully initialized cache");
        */
    }

}

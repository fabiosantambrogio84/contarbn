package com.contarbn.repository.views;

import com.contarbn.model.views.VDocumentoAcquisto;
import com.contarbn.repository.custom.VDocumentoAcquistoCustomRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface VDocumentoAcquistoRepository extends CrudRepository<VDocumentoAcquisto, Long>, VDocumentoAcquistoCustomRepository {

    @Query(nativeQuery = true,
            value = "select * from v_documento_acquisto where id in ?1 order by data_documento DESC, ragione_sociale_fornitore ASC, tipo_documento ASC, num_documento DESC"
    )
    List<VDocumentoAcquisto> findByIds(List<String> ids);

}
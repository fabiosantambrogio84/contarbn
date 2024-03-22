package com.contarbn.repository.views;

import com.contarbn.model.views.VDocumentoLast;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface VDocumentoLastRepository extends CrudRepository<VDocumentoLast, Long> {

    @Query(value = "SELECT * FROM v_documento_last WHERE context=?1", nativeQuery = true)
    Optional<VDocumentoLast> find(String context);

}

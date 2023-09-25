package com.contarbn.repository.views;

import com.contarbn.model.views.VOrdineClienteArticolo;
import org.springframework.data.repository.CrudRepository;

import java.sql.Date;
import java.util.List;

public interface VOrdineClienteArticoloRepository extends CrudRepository<VOrdineClienteArticolo, Long> {

    List<VOrdineClienteArticolo> findByIdFornitoreAndDataConsegnaBetween(Long idFornitore, Date dataDa, Date dataA);

}

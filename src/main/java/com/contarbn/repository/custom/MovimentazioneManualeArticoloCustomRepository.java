package com.contarbn.repository.custom;

import com.contarbn.model.MovimentazioneManualeArticolo;
import com.contarbn.util.enumeration.Operation;
import com.contarbn.util.enumeration.Resource;

import java.sql.Date;
import java.util.Optional;

public interface MovimentazioneManualeArticoloCustomRepository {

    Optional<MovimentazioneManualeArticolo> findLastByOperationAndContext(Operation operation, Resource resource, Long idArticolo, String lotto, Date scadenza);

}

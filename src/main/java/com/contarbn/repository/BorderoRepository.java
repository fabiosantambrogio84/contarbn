package com.contarbn.repository;

import com.contarbn.model.Bordero;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;

public interface BorderoRepository extends CrudRepository<Bordero, Long> {

    List<Bordero> findByDataInserimento(Timestamp dataInserimento);
}

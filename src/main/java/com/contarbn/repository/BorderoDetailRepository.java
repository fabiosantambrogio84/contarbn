package com.contarbn.repository;

import com.contarbn.model.BorderoDetail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Date;

public interface BorderoDetailRepository extends CrudRepository<BorderoDetail, String> {

    void deleteByIdBordero(Integer idBordero);

}

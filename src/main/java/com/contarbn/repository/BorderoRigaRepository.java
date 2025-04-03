package com.contarbn.repository;

import com.contarbn.model.BorderoRiga;
import org.springframework.data.repository.CrudRepository;

public interface BorderoRigaRepository extends CrudRepository<BorderoRiga, Long> {

    void deleteByIdBordero(Integer idBordero);

}

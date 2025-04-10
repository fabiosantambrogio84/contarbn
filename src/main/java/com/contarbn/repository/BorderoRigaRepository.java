package com.contarbn.repository;

import com.contarbn.model.BorderoRiga;
import com.contarbn.repository.custom.VBorderoRigaRepository;
import org.springframework.data.repository.CrudRepository;

public interface BorderoRigaRepository extends CrudRepository<BorderoRiga, String>, VBorderoRigaRepository {

    void deleteByIdBordero(Integer idBordero);

}

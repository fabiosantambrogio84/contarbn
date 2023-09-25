package com.contarbn.repository.views;

import com.contarbn.model.views.VDdt;
import com.contarbn.repository.custom.VDdtCustomRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface VDdtRepository extends CrudRepository<VDdt, Long>, VDdtCustomRepository {

    @Override
    Set<VDdt> findAll();

}

package com.contarbn.repository;

import com.contarbn.model.DittaInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DittaInfoRepository extends CrudRepository<DittaInfo, Long> {

    List<DittaInfo> findAllByOrderByCodiceAsc();

}

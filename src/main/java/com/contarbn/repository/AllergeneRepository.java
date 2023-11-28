package com.contarbn.repository;

import com.contarbn.model.Allergene;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AllergeneRepository extends CrudRepository<Allergene, Long> {

    List<Allergene> findAllByOrderByNomeAsc();

}

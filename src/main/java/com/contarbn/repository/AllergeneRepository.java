package com.contarbn.repository;

import com.contarbn.model.Allergene;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AllergeneRepository extends CrudRepository<Allergene, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM contarbn.allergene WHERE attivo in ?1 ORDER BY ordine, nome")
    List<Allergene> findAllByAttivo(List<Integer> activeValues);

    Optional<Allergene> findByOrdineAndIdNot(Integer ordine, Long id);
}

package com.contarbn.repository;

import com.contarbn.model.Produzione;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProduzioneRepository extends CrudRepository<Produzione, Long> {

    @Override
    Set<Produzione> findAll();

    @Query(value = "SELECT MAX(codice) + 1 FROM produzione WHERE lotto_anno = ?1", nativeQuery = true)
    Optional<Integer> findNextCodiceByLottoAnno(Integer lottoAnno);

    List<Produzione> findByRicettaId(Long idRicetta);

}
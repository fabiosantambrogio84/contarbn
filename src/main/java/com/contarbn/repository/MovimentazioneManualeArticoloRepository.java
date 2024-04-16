package com.contarbn.repository;

import com.contarbn.model.MovimentazioneManualeArticolo;
import com.contarbn.repository.custom.MovimentazioneManualeArticoloCustomRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface MovimentazioneManualeArticoloRepository extends CrudRepository<MovimentazioneManualeArticolo, Long>, MovimentazioneManualeArticoloCustomRepository {

    @Override
    Set<MovimentazioneManualeArticolo> findAll();

    Set<MovimentazioneManualeArticolo> findByArticoloIdAndLotto(Long idArticolo, String lotto);

    Set<MovimentazioneManualeArticolo> findByArticoloId(Long idArticolo);

    void deleteByArticoloIdIn(List<Long> idArticoli);
}

package com.contarbn.repository;

import com.contarbn.model.NotaAccredito;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface NotaAccreditoRepository extends CrudRepository<NotaAccredito, Long> {

    @Override
    Set<NotaAccredito> findAll();

    Set<NotaAccredito> findAllByOrderByAnnoDescProgressivoDesc();

    List<NotaAccredito> findByAnnoOrderByProgressivoDesc(Integer anno);

    Optional<NotaAccredito> findByAnnoAndProgressivoAndIdNot(Integer anno, Integer progressivo, Long idNotaAccredito);
}

package com.contarbn.repository;

import com.contarbn.model.DdtAcquistoIngrediente;
import com.contarbn.model.DdtAcquistoIngredienteKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface DdtAcquistoIngredienteRepository extends CrudRepository<DdtAcquistoIngrediente, Long> {

    @Override
    Set<DdtAcquistoIngrediente> findAll();

    Optional<DdtAcquistoIngrediente> findById(DdtAcquistoIngredienteKey id);

    Set<DdtAcquistoIngrediente> findByDdtAcquistoId(Long ddtAcquistoId);

    Set<DdtAcquistoIngrediente> findByIngredienteId(Long ingredienteId);

    Set<DdtAcquistoIngrediente> findByIngredienteIdAndLotto(Long ingredienteId, String lotto);

    void deleteByDdtAcquistoId(Long ddtAcquistoId);

    void deleteByIngredienteId(Long ingredienteId);
}

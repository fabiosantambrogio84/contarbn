package com.contarbn.repository;

import com.contarbn.model.ListinoAssociato;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ListinoAssociatoRepository extends CrudRepository<ListinoAssociato, Long> {

    @Override
    Set<ListinoAssociato> findAll();

    List<ListinoAssociato> findByClienteId(Long idCliente);

    List<ListinoAssociato> findByFornitoreId(Long idFornitore);

    Optional<ListinoAssociato> findByClienteIdAndFornitoreIdAndListinoId(Long idCliente, Long idFornitore, Long idListino);

    void deleteByClienteId(Long idCliente);

}

package com.contarbn.repository;

import com.contarbn.model.DdtArticoloOrdineCliente;
import com.contarbn.model.DdtArticoloOrdineClienteKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DdtArticoloOrdineClienteRepository extends CrudRepository<DdtArticoloOrdineCliente, DdtArticoloOrdineClienteKey> {

    @Query(nativeQuery = true,
            value = "SELECT id_ddt,id_articolo,uuid,id_ordine_cliente,data_inserimento FROM contarbn.ddt_articolo_ordine_cliente WHERE id_ddt = ?1"
    )
    List<DdtArticoloOrdineCliente> findByDdtArticoloDdtId(Long idDdt);

    List<DdtArticoloOrdineCliente> findAllByIdDdtId(Long idDdt);

    void deleteByIdDdtId(Long idDdt);

    void deleteByOrdineClienteId(Long idOrdineCliente);
}

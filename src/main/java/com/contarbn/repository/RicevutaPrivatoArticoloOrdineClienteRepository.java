package com.contarbn.repository;

import com.contarbn.model.RicevutaPrivatoArticoloOrdineCliente;
import com.contarbn.model.RicevutaPrivatoArticoloOrdineClienteKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RicevutaPrivatoArticoloOrdineClienteRepository extends CrudRepository<RicevutaPrivatoArticoloOrdineCliente, RicevutaPrivatoArticoloOrdineClienteKey> {

    @Query(nativeQuery = true,
            value = "SELECT id_ricevuta_privato,id_articolo,uuid,id_ordine_cliente,data_inserimento FROM contarbn.ricevuta_privato_ordine_cliente WHERE id_ricevuta_privato = ?1"
    )
    List<RicevutaPrivatoArticoloOrdineCliente> findByRicevutaPrivatoArticoloDdtId(Long idRicevutaPrivato);

}

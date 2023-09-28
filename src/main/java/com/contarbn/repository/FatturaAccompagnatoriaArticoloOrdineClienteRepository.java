package com.contarbn.repository;

import com.contarbn.model.FatturaAccompagnatoriaArticoloOrdineCliente;
import com.contarbn.model.FatturaAccompagnatoriaArticoloOrdineClienteKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FatturaAccompagnatoriaArticoloOrdineClienteRepository extends CrudRepository<FatturaAccompagnatoriaArticoloOrdineCliente, FatturaAccompagnatoriaArticoloOrdineClienteKey> {

    @Query(nativeQuery = true,
            value = "SELECT id_fattura_accom,id_articolo,uuid,id_ordine_cliente,data_inserimento FROM contarbn.fattura_accom_articolo_ordine_cliente WHERE id_fattura_accom = ?1"
    )
    List<FatturaAccompagnatoriaArticoloOrdineCliente> findByFatturaAccompagnatoriaArticoloDdtId(Long idFatturaAccompagnatoria);

    List<FatturaAccompagnatoriaArticoloOrdineCliente> findAllByIdFatturaAccompagnatoriaId(Long idFatturaAccompagnatoria);

    void deleteByIdFatturaAccompagnatoriaId(Long idFatturaAccompagnatoria);

    void deleteByOrdineClienteId(Long idOrdineCliente);
}

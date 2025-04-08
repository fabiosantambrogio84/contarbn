package com.contarbn.repository;

import com.contarbn.model.Bordero;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public interface BorderoRepository extends CrudRepository<Bordero, Long> {

    List<Bordero> findByDataInserimento(Timestamp dataInserimento);

    @Query(value = "CALL genera_bordero(:id_bordero, :id_autista_trasportatore, :data_consegna)", nativeQuery = true)
    String generaBordero(@Param("id_bordero") Long idBordero, @Param("id_autista_trasportatore") Integer idAutistaTrasportatore, @Param("data_consegna") Date dataConsegna);
}

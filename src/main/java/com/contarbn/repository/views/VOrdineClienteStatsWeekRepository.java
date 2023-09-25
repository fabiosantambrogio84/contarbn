package com.contarbn.repository.views;

import com.contarbn.model.views.VOrdineClienteStatsWeek;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface VOrdineClienteStatsWeekRepository extends CrudRepository<VOrdineClienteStatsWeek, Long> {

    List<VOrdineClienteStatsWeek> findByIdClienteAndIdPuntoConsegnaIn(Long idCliente, List<Long> idPuntiConsegna);

}

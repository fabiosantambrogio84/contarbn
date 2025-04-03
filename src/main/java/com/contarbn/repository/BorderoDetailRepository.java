package com.contarbn.repository;

import com.contarbn.model.BorderoDetail;
import com.contarbn.model.BorderoRiga;
import org.springframework.data.repository.CrudRepository;

public interface BorderoDetailRepository extends CrudRepository<BorderoDetail, Long> {

    void deleteByIdBordero(Integer idBordero);

    /*

select
	null as id_bordero,
	ddt.id_cliente,
	null as id_fornitore,
	ddt.id_punto_consegna,
	'ddt' as tipo_documento,
	ddt.id as id_documento,
	ddt.data_trasporto,
	ddt.note,
	current_timestamp() as data_inserimento
from contarbn.ddt
where
	(ddt.consegnato is null or ddt.consegnato = false) and
	ddt.id_autista = 3 and
	ddt.data_trasporto <= now()
union all
select
	null as id_bordero,
	fattura_accom.id_cliente,
	null as id_fornitore,
	fattura_accom.id_punto_consegna,
	'fattura_accompagnatoria' as tipo_documento,
	fattura_accom.id as id_documento,
	fattura_accom.data_trasporto,
	fattura_accom.note,
	current_timestamp() as data_inserimento
from contarbn.fattura_accom
where
	(fattura_accom.consegnato is null or fattura_accom.consegnato = false) and
	fattura_accom.id_trasportatore = 1 and
	fattura_accom.data_trasporto <= now()
union all
select
	null as id_bordero,
	ricevuta_privato.id_cliente,
	null as id_fornitore,
	ricevuta_privato.id_punto_consegna,
	'ricevuta_privato' as tipo_documento,
	ricevuta_privato.id as id_documento,
	ricevuta_privato.data_trasporto,
	ricevuta_privato.note,
	current_timestamp() as data_inserimento
from contarbn.ricevuta_privato
where
	(ricevuta_privato.consegnato is null or ricevuta_privato.consegnato = false) and
	ricevuta_privato.id_trasportatore = 1 and
	ricevuta_privato.data_trasporto <= now()
union all
select
	null as id_bordero,
	null as id_cliente,
	nota_reso.id_fornitore,
	null as id_punto_consegna,
	'nota_reso' as tipo_documento,
	nota_reso.id as id_documento,
	nota_reso.data_trasporto,
	nota_reso.note,
	current_timestamp() as data_inserimento
from contarbn.nota_reso
where
	(nota_reso.consegnato is null or nota_reso.consegnato = false) and
	nota_reso.id_trasportatore = 1 and
	nota_reso.data_trasporto <= now()

     */
}

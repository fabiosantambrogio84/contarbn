create or replace algorithm = UNDEFINED view contarbn.v_scheda_tecnica_light as
select
    id,
    id_produzione,
    id_articolo,
    codice_prodotto,
    prodotto,
    concat(codice_prodotto, ' ', prodotto) as prodotto_descr,
    num_revisione,
    data
from contarbn.scheda_tecnica
;

--
alter table contarbn.fattura_accom_acquisto add column id_trasportatore int unsigned after ora_trasporto;
alter table contarbn.fattura_accom_acquisto add CONSTRAINT `fk_fattura_accom_acq_trasportatore` FOREIGN KEY (`id_trasportatore`) REFERENCES `trasportatore` (`id`);

alter table contarbn.fattura_accom_acquisto drop column trasportatore;


ALTER TABLE contarbn.ddt MODIFY COLUMN tipo_trasporto varchar(100) CHARACTER SET latin1 COLLATE latin1_general_cs NULL;

create or replace algorithm = UNDEFINED view `v_fattura_accom_last` as
select
    fattura_accom.`id` as `id`,
    null as `id_autista`,
    fattura_accom.`id_trasportatore` as `id_trasportatore`,
    fattura_accom.`tipo_trasporto` as `tipo_trasporto`
from
    fattura_accom
order by
    coalesce(fattura_accom.`data_aggiornamento`, fattura_accom.`data_inserimento`) desc
    limit 1
;

create or replace algorithm = UNDEFINED view `v_ricevuta_privato_last` as
select
    ricevuta_privato.`id` as `id`,
    ricevuta_privato.`id_autista`,
    ricevuta_privato.`id_trasportatore` as `id_trasportatore`,
    ricevuta_privato.`tipo_trasporto` as `tipo_trasporto`
from
    ricevuta_privato
order by
    coalesce(ricevuta_privato.`data_aggiornamento`, ricevuta_privato.`data_inserimento`) desc
    limit 1
;

create or replace algorithm = UNDEFINED view `v_fattura_accom_acquisto_last` as
select
    fattura_accom_acquisto.`id` as `id`,
    null as `id_autista`,
    fattura_accom_acquisto.`id_trasportatore` as `id_trasportatore`,
    fattura_accom_acquisto.`tipo_trasporto` as `tipo_trasporto`
from
    fattura_accom_acquisto
order by
    coalesce(fattura_accom_acquisto.`data_aggiornamento`, fattura_accom_acquisto.`data_inserimento`) desc
    limit 1
;

create or replace algorithm = UNDEFINED view `v_documento_last` as
select
    'ddt' as context,
    v_ddt_last.`id` as `id`,
    v_ddt_last.`id_autista` as `id_autista`,
    v_ddt_last.`id_trasportatore` as `id_trasportatore`,
    v_ddt_last.`tipo_trasporto` as `tipo_trasporto`
from
    v_ddt_last
union all
select
    'fattura-accompagnatoria' as context,
    v_fattura_accom_last.`id` as `id`,
    v_fattura_accom_last.`id_autista`,
    v_fattura_accom_last.`id_trasportatore` as `id_trasportatore`,
    v_fattura_accom_last.`tipo_trasporto` as `tipo_trasporto`
from
    v_fattura_accom_last
union all
select
    'ricevuta-privato' as context,
    v_ricevuta_privato_last.`id` as `id`,
    v_ricevuta_privato_last.`id_autista`,
    v_ricevuta_privato_last.`id_trasportatore` as `id_trasportatore`,
    v_ricevuta_privato_last.`tipo_trasporto` as `tipo_trasporto`
from
    v_ricevuta_privato_last
union all
select
    'fattura-accompagnatoria-acquisto' as context,
    v_fattura_accom_acquisto_last.`id` as `id`,
    v_fattura_accom_acquisto_last.`id_autista`,
    v_fattura_accom_acquisto_last.`id_trasportatore` as `id_trasportatore`,
    v_fattura_accom_acquisto_last.`tipo_trasporto` as `tipo_trasporto`
from
    v_fattura_accom_acquisto_last
;

-- GIACENZE
/*
alter table contarbn.movimentazione_manuale_articolo CHANGE quantita quantita_old decimal(10,3) NULL;
alter table contarbn.movimentazione_manuale_articolo add column pezzi int after scadenza;
alter table contarbn.movimentazione_manuale_articolo add column quantita decimal(10,3) after pezzi;
alter table contarbn.movimentazione_manuale_articolo add column operation varchar(255) after quantita;
alter table contarbn.movimentazione_manuale_articolo add column context varchar(255) after operation;
alter table contarbn.movimentazione_manuale_articolo add column id_documento int unsigned after context;
alter table contarbn.movimentazione_manuale_articolo add column num_documento varchar(255) after id_documento;
alter table contarbn.movimentazione_manuale_articolo add column anno_documento int after num_documento;
alter table contarbn.movimentazione_manuale_articolo add column fornitore_documento varchar(255) after anno_documento;

update contarbn.movimentazione_manuale_articolo set pezzi = round(quantita_old), operation='CREATE';
*/

-- alter table contarbn.movimentazione_manuale_articolo drop column quantita_old;





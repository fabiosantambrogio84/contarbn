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

-- alter table contarbn.movimentazione_manuale_articolo drop column quantita_old;

alter table contarbn.giacenza_articolo CHANGE quantita quantita_old decimal(10,3) NULL;
alter table contarbn.giacenza_articolo add column pezzi int after scadenza;
alter table contarbn.giacenza_articolo add column quantita decimal(10,3) after pezzi;

update contarbn.giacenza_articolo set pezzi = round(quantita_old);
-- alter table contarbn.giacenza_articolo drop column quantita_old;

update contarbn.articolo set id_unita_misura = 2 where id_unita_misura is null;

create or replace algorithm = UNDEFINED view `v_giacenza_articolo` as
select
    `giacenza_articolo`.`id` as `id`,
    `giacenza_articolo`.`id_articolo` as `id_articolo`,
    concat(`articolo`.`codice`, ' ', coalesce(`articolo`.`descrizione`, '')) as `articolo`,
    `articolo`.`prezzo_acquisto` as `prezzo_acquisto`,
    `articolo`.`prezzo_listino_base` as `prezzo_listino_base`,
    unita_misura.etichetta as unita_misura,
    `giacenza_articolo`.`quantita` as `quantita`,
       giacenza_articolo.pezzi as pezzi,
    case
        when unita_misura.nome = 'pz' then
            giacenza_articolo.pezzi
        else
            giacenza_articolo.quantita
        end as quantita_result,
     case
         when unita_misura.nome = 'pz' then
             giacenza_articolo.pezzi * articolo.prezzo_acquisto
         else
             giacenza_articolo.quantita * articolo.prezzo_acquisto
         end as totale,
    `articolo`.`attivo` as `attivo`,
    `articolo`.`id_fornitore` as `id_fornitore`,
    `fornitore`.`ragione_sociale` as `fornitore`,
    `giacenza_articolo`.`lotto` as `lotto`,
    `giacenza_articolo`.`scadenza` as `scadenza`,
    `articolo`.`scadenza_giorni_allarme` as `scadenza_giorni_allarme`,
    (case
         when (curdate() >= (`giacenza_articolo`.`scadenza` - interval coalesce(`articolo`.`scadenza_giorni_allarme`, 0) day)) then 1
         else 0
        end) as `scaduto`
from giacenza_articolo
join articolo on
    giacenza_articolo.id_articolo = articolo.id
join unita_misura on
    articolo.id_unita_misura = unita_misura.id
left join fornitore on
    articolo.id_fornitore = fornitore.id
;

create or replace algorithm = UNDEFINED view `v_giacenza_articolo_agg` as
select
    `v_giacenza_articolo`.`id_articolo` as `id_articolo`,
    `v_giacenza_articolo`.`articolo` as `articolo`,
    `v_giacenza_articolo`.`prezzo_acquisto` as `prezzo_acquisto`,
    `v_giacenza_articolo`.`prezzo_listino_base` as `prezzo_listino_base`,
    `v_giacenza_articolo`.`attivo` as `attivo`,
    `v_giacenza_articolo`.`id_fornitore` as `id_fornitore`,
    `v_giacenza_articolo`.`fornitore` as `fornitore`,
    v_giacenza_articolo.unita_misura,
    sum(`v_giacenza_articolo`.`quantita`) as `quantita`,
    cast(sum(`v_giacenza_articolo`.pezzi) as signed) as pezzi,
    sum(`v_giacenza_articolo`.`quantita_result`) as `quantita_result`,
    sum(`v_giacenza_articolo`.`totale`) as totale,
    (case
         when (sum(`v_giacenza_articolo`.`scaduto`) > 0) then 1
         else 0
        end) as `scaduto`
from
    `v_giacenza_articolo`
group by
    `v_giacenza_articolo`.`id_articolo`,
    `v_giacenza_articolo`.`articolo`,
    `v_giacenza_articolo`.`prezzo_acquisto`,
    `v_giacenza_articolo`.`prezzo_listino_base`,
    `v_giacenza_articolo`.`attivo`,
    `v_giacenza_articolo`.`id_fornitore`,
    `v_giacenza_articolo`.`fornitore`,
    v_giacenza_articolo.unita_misura
;

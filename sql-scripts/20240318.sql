create or replace
algorithm = UNDEFINED view `v_ddt_last` as
select
    `ddt`.`id`,
    `ddt`.`id_autista`,
    ddt.id_trasportatore,
    ddt.tipo_trasporto
from
    `ddt`
order by
    coalesce(`ddt`.`data_aggiornamento`, `ddt`.`data_inserimento`) desc
    limit 1;

alter table contarbn.fattura_accom add column id_trasportatore int unsigned after ora_trasporto;
alter table contarbn.fattura_accom add CONSTRAINT `fk_fattura_accom_trasportatore` FOREIGN KEY (`id_trasportatore`) REFERENCES `trasportatore` (`id`);

alter table contarbn.ricevuta_privato add column id_trasportatore int unsigned after ora_trasporto;
alter table contarbn.ricevuta_privato add CONSTRAINT `fk_ricevuta_pvt_trasportatore` FOREIGN KEY (`id_trasportatore`) REFERENCES `trasportatore` (`id`);

alter table contarbn.ddt drop column trasportatore;
alter table contarbn.fattura_accom drop column trasportatore;
alter table contarbn.ricevuta_privato drop column trasportatore;

-- SCHEDE TECNICHE
alter table contarbn.scheda_tecnica add column pdf blob after imballo_dimensioni;

create or replace
algorithm = UNDEFINED view `v_produzione` as
select
    uuid() as `id`,
    `produzione`.`id` as `id_produzione`,
    `produzione`.`codice` as `codice_produzione`,
    `produzione`.`data_produzione` as `data_produzione`,
    `produzione`.`tipologia` as `tipologia`,
    `produzione_confezione`.`id_confezione` as `id_confezione`,
    `produzione`.`lotto` as `lotto`,
    `produzione`.`scadenza` as `scadenza`,
    `produzione_confezione`.`id_articolo` as `id_articolo`,
    `articolo`.`codice` as `codice_articolo`,
    `articolo`.`descrizione` as `descrizione_articolo`,
    `produzione_confezione`.`id_ingrediente` as `id_ingrediente`,
    `ingrediente`.`codice` as `codice_ingrediente`,
    `ingrediente`.`descrizione` as `descrizione_ingrediente`,
    `produzione_confezione`.`num_confezioni_prodotte` as `num_confezioni_prodotte`,
    `produzione`.`quantita_totale` as `quantita`,
    concat(`ricetta`.`codice`, ' ', `ricetta`.`nome`) as `ricetta`,
    `produzione`.`barcode_ean_13` as `barcode_ean_13`,
    `produzione`.`barcode_ean_128` as `barcode_ean_128`,
    scheda_tecnica.id as id_scheda_tecnica
from
    `produzione_confezione`
        join `produzione` on
            `produzione_confezione`.`id_produzione` = `produzione`.`id`
        join `ricetta` on
            `produzione`.`id_ricetta` = `ricetta`.`id`
        left join `articolo` on
            `produzione_confezione`.`id_articolo` = `articolo`.`id`
        left join `ingrediente` on
            `produzione_confezione`.`id_ingrediente` = `ingrediente`.`id`
        left join scheda_tecnica on
                scheda_tecnica.id_produzione = produzione.id and
                scheda_tecnica.id_articolo = produzione_confezione.id_articolo
;
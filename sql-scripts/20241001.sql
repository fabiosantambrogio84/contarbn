ALTER TABLE ingrediente MODIFY codice varchar(100) CHARACTER SET latin1 COLLATE latin1_general_cs;
ALTER TABLE ingrediente MODIFY note text CHARACTER SET latin1 COLLATE latin1_general_cs;

alter table contarbn.produzione_confezione drop primary key;
alter table contarbn.produzione_confezione add column id int unsigned NOT NULL AUTO_INCREMENT primary key first;
alter table contarbn.produzione_confezione add column `barcode` varchar(255) after num_confezioni;

create or replace algorithm = UNDEFINED view contarbn.`v_produzione` as
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
    `produzione_confezione`.`barcode` as `barcode_ean_13`,
    `produzione`.`barcode_ean_128` as `barcode_ean_128`,
       produzione_confezione.id as id_produzione_confezione,
    `scheda_tecnica`.`id` as `id_scheda_tecnica`
from
    (((((`produzione_confezione`
        join `produzione` on
            ((`produzione_confezione`.`id_produzione` = `produzione`.`id`)))
        join `ricetta` on
            ((`produzione`.`id_ricetta` = `ricetta`.`id`)))
        left join `articolo` on
            ((`produzione_confezione`.`id_articolo` = `articolo`.`id`)))
        left join `ingrediente` on
            ((`produzione_confezione`.`id_ingrediente` = `ingrediente`.`id`)))
        left join `scheda_tecnica` on
        (((`scheda_tecnica`.`id_produzione` = `produzione`.`id`)
            and (`scheda_tecnica`.`id_articolo` = `produzione_confezione`.`id_articolo`))));

drop view contarbn.v_produzione_etichetta_ingrediente_old;
drop view contarbn.v_produzione_etichetta_sub_old;

RENAME TABLE contarbn.v_produzione_etichetta_sub TO contarbn.old_v_produzione_etichetta_sub;
RENAME TABLE contarbn.v_produzione_etichetta TO contarbn.old_v_produzione_etichetta;

create or replace algorithm = UNDEFINED view contarbn.`v_produzione_confezione_etichetta_sub` as
select
    `produzione_confezione`.`id` as `id`,
     produzione_confezione.id_produzione as id_produzione,
    `produzione`.`lotto` as `lotto`,
    `produzione`.`scadenza` as `scadenza`,
    `produzione_confezione`.`barcode` as `barcode_ean_13`,
    `produzione`.`barcode_ean_128` as `barcode_ean_128`,
    coalesce(`articolo`.`descrizione`, `v_ricetta`.`nome_ricetta`) as `articolo`,
    `v_ricetta`.`valori_nutrizionali` as `valori_nutrizionali`,
    `v_ricetta`.`conservazione` as `conservazione`,
    `ingrediente`.`descrizione` as `descrizione`,
    `produzione_ingrediente`.`percentuale` as `percentuale`,
    (case
         when (`v_ingrediente_allergeni`.`allergeni` is null) then concat(`ingrediente`.`descrizione`, ' ', `produzione_ingrediente`.`percentuale`, '%')
         else concat(concat(`ingrediente`.`descrizione`, ' ', `produzione_ingrediente`.`percentuale`, '%'), ' (contiene <b>', lower(`v_ingrediente_allergeni`.`allergeni`), '</b>)')
        end) as `ingrediente_descrizione`,
    concat(cast('Può contenere tracce di:' as char charset latin1), convert(ifnull(lower(`v_ricetta`.`tracce`), '') using latin1)) as `tracce`,
    confezione.peso/1000 as peso_kg,
    produzione_confezione.lotto as lotto_confezione
from
    contarbn.produzione_confezione
join contarbn.produzione on
    produzione_confezione.id_produzione = produzione.id
join contarbn.confezione on
    produzione_confezione.id_confezione = confezione.id
join contarbn.`v_ricetta` on
    `produzione`.`id_ricetta` = `v_ricetta`.`id_ricetta`
left join contarbn.`articolo` on
    `produzione`.`id_articolo` = `articolo`.`id`
left join contarbn.`produzione_ingrediente` on
    `produzione`.`id` = `produzione_ingrediente`.`id_produzione`
left join contarbn.`ingrediente` on
    `ingrediente`.`id` = `produzione_ingrediente`.`id_ingrediente`
left join contarbn.`v_ingrediente_allergeni` on
`ingrediente`.`id` = `v_ingrediente_allergeni`.`id`
;

create or replace algorithm = UNDEFINED view contarbn.`v_produzione_confezione_etichetta` as
select
    `p`.`id` as `id`,
    p.id_produzione,
    `p`.`lotto` as `lotto`,
    `p`.`scadenza` as `scadenza`,
    `p`.`barcode_ean_13` as `barcode_ean_13`,
    `p`.`barcode_ean_128` as `barcode_ean_128`,
    `p`.`articolo` as `articolo`,
    group_concat(distinct `p`.`ingrediente_descrizione` order by `p`.`percentuale` desc, `p`.`descrizione` asc separator ',') as `ingredienti`,
    `p`.`tracce` as `ingredienti_2`,
    `p`.`valori_nutrizionali` as `valori_nutrizionali`,
    `p`.`conservazione` as `conservazione`,
    p.peso_kg,
    p.lotto_confezione
from
    contarbn.`v_produzione_confezione_etichetta_sub` `p`
group by
    `p`.`id`,
    p.id_produzione,
    `p`.`lotto`,
    `p`.`scadenza`,
    `p`.`barcode_ean_13`,
    `p`.`barcode_ean_128`,
    `p`.`articolo`,
    `p`.`valori_nutrizionali`,
    `p`.`conservazione`,
    `p`.`tracce`,
    p.peso_kg,
    p.lotto_confezione;
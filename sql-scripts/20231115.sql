
ALTER TABLE contarbn.produzione DROP COLUMN lotto_film_chiusura_OLD;

ALTER TABLE contarbn.produzione_confezione ADD lotto_2 varchar(100) DEFAULT NULL after lotto;

create or replace
algorithm = UNDEFINED view `v_produzione_confezione` as
select
    uuid() as `id`,
    `produzione`.`id` as `id_produzione`,
    `produzione`.`codice` as `codice_produzione`,
    `produzione`.`data_produzione` as `data_produzione`,
    `produzione`.`tipologia` as `tipologia`,
    `produzione_confezione`.`id_confezione` as `id_confezione`,
    `produzione_confezione`.`num_confezioni` as `num_confezioni`,
    `produzione_confezione`.`num_confezioni_prodotte` as `num_confezioni_prodotte`,
    `produzione_confezione`.`lotto` as `lotto_confezione`,
    `produzione_confezione`.`lotto_2` as `lotto_confezione_2`,
    `produzione_confezione`.`lotto_film_chiusura`,
    `confezione`.`tipo` as `tipo_confezione`,
    `produzione`.`lotto` as `lotto_produzione`,
    `produzione`.`scadenza` as `scadenza`,
    `produzione_confezione`.`id_articolo` as `id_articolo`,
    `articolo`.`codice` as `codice_articolo`,
    `articolo`.`descrizione` as `descrizione_articolo`,
    `produzione_confezione`.`id_ingrediente` as `id_ingrediente`,
    `ingrediente`.`codice` as `codice_ingrediente`,
    `ingrediente`.`descrizione` as `descrizione_ingrediente`,
    `produzione`.`quantita_totale` as `quantita`,
    concat(`ricetta`.`codice`, ' ', `ricetta`.`nome`) as `ricetta`
from
    (((((`produzione`
join `produzione_confezione` on
    ((`produzione`.`id` = `produzione_confezione`.`id_produzione`)))
join `confezione` on
    ((`produzione_confezione`.`id_confezione` = `confezione`.`id`)))
join `ricetta` on
    ((`produzione`.`id_ricetta` = `ricetta`.`id`)))
left join `articolo` on
    ((`produzione_confezione`.`id_articolo` = `articolo`.`id`)))
left join `ingrediente` on
((`produzione_confezione`.`id_ingrediente` = `ingrediente`.`id`)));
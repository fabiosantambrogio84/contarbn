alter table contarbn.produzione_confezione add `lotto_film_chiusura` varchar(100) DEFAULT NULL after lotto_produzione;

-- update data
update contarbn.produzione_confezione pc
inner join (
    select
        pc.id_produzione,
        pc.id_confezione,
        p.lotto_film_chiusura
    from contarbn.produzione p
    join contarbn.produzione_confezione pc on
        p.id = pc.id_produzione
    ) t on
        pc.id_produzione = t.id_produzione and
        pc.id_confezione = t.id_confezione
    set
        pc.lotto_film_chiusura = t.lotto_film_chiusura
;

alter table contarbn.produzione drop column film_chiusura;
--alter table contarbn.produzione drop column lotto_film_chiusura;

ALTER TABLE contarbn.produzione CHANGE lotto_film_chiusura lotto_film_chiusura_OLD varchar(100) CHARACTER SET latin1 COLLATE latin1_general_cs NULL;
ALTER TABLE contarbn.produzione MODIFY COLUMN lotto_film_chiusura_OLD varchar(100) CHARACTER SET latin1 COLLATE latin1_general_cs NULL;

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
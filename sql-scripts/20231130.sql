create or replace algorithm = UNDEFINED view `v_produzione_etichetta_ingrediente_old` as
select
    `produzione_ingrediente`.`id_produzione` as `id_produzione`,
    `produzione_ingrediente`.`id_ingrediente` as `id_ingrediente`,
    `ingrediente`.`descrizione` as `descrizione`,
    `produzione_ingrediente`.`percentuale` as `percentuale`,
    (case
         when (`produzione_ingrediente`.`percentuale` is null) then `ingrediente`.`descrizione`
         else concat(`ingrediente`.`descrizione`, ' ', `produzione_ingrediente`.`percentuale`, '%')
        end) as `ingrediente_descrizione`,
    group_concat(distinct `allergene`.`nome` order by `allergene`.`nome` desc separator ',') as `allergeni`
from
    `produzione_ingrediente`
join `ingrediente` on
    `produzione_ingrediente`.`id_ingrediente` = `ingrediente`.`id`
left join `ingrediente_allergene` on
    `ingrediente`.`id` = `ingrediente_allergene`.`id_ingrediente`
left join `allergene` on
    `allergene`.`id` = `ingrediente_allergene`.`id_allergene`
group by
    `produzione_ingrediente`.`id_produzione`,
    `produzione_ingrediente`.`id_ingrediente`,
    `ingrediente`.`descrizione`,
    `produzione_ingrediente`.`percentuale`;

drop view contarbn.v_produzione_etichetta_ingrediente;

create or replace algorithm = UNDEFINED view `v_produzione_etichetta_sub_OLD` as
select
    `produzione`.`id` as `id`,
    `produzione`.`lotto` as `lotto`,
    `produzione`.`scadenza` as `scadenza`,
    `produzione`.`barcode_ean_13` as `barcode_ean_13`,
    `produzione`.`barcode_ean_128` as `barcode_ean_128`,
    coalesce(`articolo`.`descrizione`, `v_ricetta`.`nome_ricetta`) as `articolo`,
    `v_ricetta`.`valori_nutrizionali` as `valori_nutrizionali`,
    `v_ricetta`.`conservazione` as `conservazione`,
    `v_produzione_etichetta_ingrediente_old`.`descrizione` as `descrizione`,
    `v_produzione_etichetta_ingrediente_old`.`percentuale` as `percentuale`,
    (case
         when (`v_produzione_etichetta_ingrediente_old`.`allergeni` is null) then `v_produzione_etichetta_ingrediente_old`.`ingrediente_descrizione`
         else concat(`v_produzione_etichetta_ingrediente_old`.`ingrediente_descrizione`, ' (contiene <b>', lower(`v_produzione_etichetta_ingrediente_old`.`allergeni`), '</b>)')
        end) as `ingrediente_descrizione`,
    concat(cast('Può contenere tracce di:' as char charset latin1), convert(ifnull(lower(`v_ricetta`.`tracce`), '') using latin1)) as `tracce`
from
    `produzione`
join `v_ricetta` on
    `produzione`.`id_ricetta` = `v_ricetta`.`id_ricetta`
left join `articolo` on
    `produzione`.`id_articolo` = `articolo`.`id`
left join `v_produzione_etichetta_ingrediente_old` on
    `produzione`.`id` = `v_produzione_etichetta_ingrediente_old`.`id_produzione`;

create or replace view contarbn.v_ingrediente_allergeni as
select
    ingrediente.id,
    ingrediente.descrizione,
    lower(group_concat(distinct allergene.nome order by allergene.nome desc separator ',')) as allergeni
from contarbn.ingrediente
join ingrediente_allergene on
    ingrediente.id = ingrediente_allergene.id_ingrediente
join allergene on
    allergene.id = ingrediente_allergene.id_allergene
group by
    ingrediente.id,
    ingrediente.descrizione;

create or replace view contarbn.v_produzione_etichetta_sub as
select
    `produzione`.`id` as `id`,
    `produzione`.`lotto` as `lotto`,
    `produzione`.`scadenza` as `scadenza`,
    `produzione`.`barcode_ean_13` as `barcode_ean_13`,
    `produzione`.`barcode_ean_128` as `barcode_ean_128`,
    coalesce(`articolo`.`descrizione`, `v_ricetta`.`nome_ricetta`) as `articolo`,
    `v_ricetta`.`valori_nutrizionali` as `valori_nutrizionali`,
    `v_ricetta`.`conservazione` as `conservazione`,
    ingrediente.`descrizione` as `descrizione`,
    produzione_ingrediente.`percentuale` as `percentuale`,
    (case
         when (v_ingrediente_allergeni.`allergeni` is null) then
             concat(`ingrediente`.`descrizione`, ' ', `produzione_ingrediente`.`percentuale`, '%')
         else
             concat(concat(`ingrediente`.`descrizione`, ' ', `produzione_ingrediente`.`percentuale`, '%'), ' (contiene <b>', lower(v_ingrediente_allergeni.`allergeni`), '</b>)')
        end) as `ingrediente_descrizione`,
    concat(cast('Può contenere tracce di:' as char charset latin1), convert(ifnull(lower(`v_ricetta`.`tracce`), '') using latin1)) as `tracce`
from
    `produzione`
join `v_ricetta` on
    `produzione`.`id_ricetta` = `v_ricetta`.`id_ricetta`
left join `articolo` on
    `produzione`.`id_articolo` = `articolo`.`id`
left join produzione_ingrediente on
    produzione.id = produzione_ingrediente.id_produzione
left join ingrediente on
    ingrediente.id = produzione_ingrediente.id_ingrediente
left join v_ingrediente_allergeni on
    ingrediente.id = v_ingrediente_allergeni.id
;
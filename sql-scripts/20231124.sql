
ALTER TABLE contarbn.produzione ADD `id_articolo` int DEFAULT NULL after id_categoria;

create or replace
algorithm = UNDEFINED view `v_produzione_etichetta_sub` as
select
    `produzione`.`id` as `id`,
    `produzione`.`lotto` as `lotto`,
    `produzione`.`scadenza` as `scadenza`,
    `produzione`.`barcode_ean_13` as `barcode_ean_13`,
    `produzione`.`barcode_ean_128` as `barcode_ean_128`,
    coalesce(articolo.descrizione,`ricetta`.`nome`) as `articolo`,
    `ricetta`.`valori_nutrizionali` as `valori_nutrizionali`,
    `ricetta`.`conservazione` as `conservazione`,
    `ingrediente`.`descrizione` as `descrizione`,
    `produzione_ingrediente`.`percentuale` as `percentuale`,
    (case
         when (`produzione_ingrediente`.`percentuale` is null) then `ingrediente`.`descrizione`
         else concat(`ingrediente`.`descrizione`, ' ', `produzione_ingrediente`.`percentuale`, '%')
        end) as `ingrediente_descrizione`
from
    (((`produzione`
        join `ricetta` on
            ((`produzione`.`id_ricetta` = `ricetta`.`id`)))
        left join articolo on
                produzione.id_articolo = articolo.id
        left join `produzione_ingrediente` on
            ((`produzione`.`id` = `produzione_ingrediente`.`id_produzione`)))
        left join `ingrediente` on
        ((`produzione_ingrediente`.`id_ingrediente` = `ingrediente`.`id`)));


create or replace
algorithm = UNDEFINED view `v_produzione_etichetta` as
select
    `p`.`id` as `id`,
    `p`.`lotto` as `lotto`,
    `p`.`scadenza` as `scadenza`,
    `p`.`barcode_ean_13` as `barcode_ean_13`,
    `p`.`barcode_ean_128` as `barcode_ean_128`,
    `p`.`articolo` as `articolo`,
    group_concat(distinct `p`.`ingrediente_descrizione` order by `p`.`percentuale` desc, `p`.`descrizione` asc separator ',') as `ingredienti`,
    `p`.`valori_nutrizionali` as `valori_nutrizionali`,
    `p`.`conservazione` as `conservazione`
from
    contarbn.v_produzione_etichetta_sub as `p`
group by
    `p`.`id`,
    `p`.`lotto`,
    `p`.`scadenza`,
    `p`.`barcode_ean_13`,
    `p`.`barcode_ean_128`,
    `p`.`articolo`,
    `p`.`valori_nutrizionali`,
    `p`.`conservazione`;


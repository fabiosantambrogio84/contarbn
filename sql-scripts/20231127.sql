CREATE TABLE contarbn.`allergene` (
   `id` int unsigned NOT NULL AUTO_INCREMENT,
   `nome` varchar(255) DEFAULT NULL,
   `data_inserimento` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
   `data_aggiornamento` timestamp NULL DEFAULT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;


INSERT INTO contarbn.allergene (id, nome, data_inserimento, data_aggiornamento)
VALUES(1, 'Anidride solforosa e solfiti', now(), now());

INSERT INTO contarbn.allergene (id, nome, data_inserimento, data_aggiornamento)
VALUES(2, 'Arachidi', now(), now());

INSERT INTO contarbn.allergene (id, nome, data_inserimento, data_aggiornamento)
VALUES(3, 'Cereali', now(), now());

INSERT INTO contarbn.allergene (id, nome, data_inserimento, data_aggiornamento)
VALUES(4, 'Crostacei', now(), now());

INSERT INTO contarbn.allergene (id, nome, data_inserimento, data_aggiornamento)
VALUES(5, 'Frutta a guscio', now(), now());

INSERT INTO contarbn.allergene (id, nome, data_inserimento, data_aggiornamento)
VALUES(6, 'Latte', now(), now());

INSERT INTO contarbn.allergene (id, nome, data_inserimento, data_aggiornamento)
VALUES(7, 'Lupini', now(), now());

INSERT INTO contarbn.allergene (id, nome, data_inserimento, data_aggiornamento)
VALUES(8, 'Molluschi', now(), now());

INSERT INTO contarbn.allergene (id, nome, data_inserimento, data_aggiornamento)
VALUES(9, 'Pesce', now(), now());

INSERT INTO contarbn.allergene (id, nome, data_inserimento, data_aggiornamento)
VALUES(10, 'Sedano', now(), now());

INSERT INTO contarbn.allergene (id, nome, data_inserimento, data_aggiornamento)
VALUES(11, 'Semi di sesamo', now(), now());

INSERT INTO contarbn.allergene (id, nome, data_inserimento, data_aggiornamento)
VALUES(12, 'Senape', now(), now());

INSERT INTO contarbn.allergene (id, nome, data_inserimento, data_aggiornamento)
VALUES(13, 'Soia', now(), now());

INSERT INTO contarbn.allergene (id, nome, data_inserimento, data_aggiornamento)
VALUES(14, 'Uova', now(), now());

-- ########################################################################

CREATE TABLE `ricetta_allergene` (
   `id_ricetta` int unsigned NOT NULL,
   `id_allergene` int unsigned NOT NULL,
   PRIMARY KEY (`id_ricetta`,`id_allergene`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE `ingrediente_allergene` (
    `id_ingrediente` int unsigned NOT NULL,
    `id_allergene` int unsigned NOT NULL,
    PRIMARY KEY (`id_ingrediente`,`id_allergene`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

ALTER TABLE contarbn.ingrediente COLLATE=latin1_general_cs;
ALTER TABLE contarbn.ricetta COLLATE=latin1_general_cs;
ALTER TABLE contarbn.ricetta_ingrediente COLLATE=latin1_general_cs;
ALTER TABLE contarbn.ingrediente MODIFY COLUMN descrizione text CHARACTER SET latin1 COLLATE latin1_general_cs NULL;


-- ########################################################################

create or replace view contarbn.v_ricetta as
select
    ricetta.id as id_ricetta,
    ricetta.nome as nome_ricetta,
    ricetta.valori_nutrizionali,
    ricetta.conservazione,
    group_concat(distinct allergene.nome order by allergene.nome desc separator ',') as tracce
from contarbn.ricetta
left join ricetta_allergene on
    ricetta_allergene.id_ricetta = ricetta.id
left join allergene on
    ricetta_allergene.id_allergene = allergene.id
group by
    ricetta.id,
    ricetta.nome,
    ricetta.valori_nutrizionali,
    ricetta.conservazione
;

create or replace view contarbn.v_produzione_etichetta_ingrediente as
select
    produzione_ingrediente.id_produzione,
    produzione_ingrediente.id_ingrediente,
    ingrediente.descrizione,
    produzione_ingrediente.percentuale,
    (case
         when (`produzione_ingrediente`.`percentuale` is null) then `ingrediente`.`descrizione`
         else concat(`ingrediente`.`descrizione`, ' ', `produzione_ingrediente`.`percentuale`, '%')
        end) as ingrediente_descrizione,
    group_concat(distinct `allergene`.`nome` order by `allergene`.`nome` desc separator ',') as allergeni
from
    contarbn.produzione_ingrediente
        join ingrediente on
            produzione_ingrediente.id_ingrediente = ingrediente.id
        left join ingrediente_allergene on
            ingrediente.id = ingrediente_allergene.id_ingrediente
        left join allergene on
            allergene.id = ingrediente_allergene.id_allergene
group by
    produzione_ingrediente.id_produzione,
    produzione_ingrediente.id_ingrediente,
    ingrediente.descrizione,
    produzione_ingrediente.percentuale
;

create or replace algorithm = UNDEFINED view contarbn.`v_produzione_etichetta_sub` as
select
    `produzione`.`id` as `id`,
    `produzione`.`lotto` as `lotto`,
    `produzione`.`scadenza` as `scadenza`,
    `produzione`.`barcode_ean_13` as `barcode_ean_13`,
    `produzione`.`barcode_ean_128` as `barcode_ean_128`,
    coalesce(`articolo`.`descrizione`, `v_ricetta`.`nome_ricetta`) as `articolo`,
    `v_ricetta`.`valori_nutrizionali` as `valori_nutrizionali`,
    `v_ricetta`.`conservazione` as `conservazione`,
    v_produzione_etichetta_ingrediente.descrizione,
    v_produzione_etichetta_ingrediente.`percentuale`,
    case
        when v_produzione_etichetta_ingrediente.allergeni is null then
            v_produzione_etichetta_ingrediente.ingrediente_descrizione
        else
            concat(v_produzione_etichetta_ingrediente.ingrediente_descrizione,' (contiene <b>', lower(v_produzione_etichetta_ingrediente.allergeni), '</b>)')
        end as ingrediente_descrizione,
    concat(cast('Può contenere tracce di:' AS CHAR CHARACTER SET latin1), ifnull(lower(v_ricetta.tracce),'')) as tracce
from
    `produzione`
        join `v_ricetta` on
            `produzione`.`id_ricetta` = `v_ricetta`.`id_ricetta`
        left join `articolo` on
            `produzione`.`id_articolo` = `articolo`.`id`
        left join v_produzione_etichetta_ingrediente on
            produzione.id = v_produzione_etichetta_ingrediente.id_produzione
;


create or replace algorithm = UNDEFINED view contarbn.`v_produzione_etichetta` as
select
    `p`.`id` as `id`,
    `p`.`lotto` as `lotto`,
    `p`.`scadenza` as `scadenza`,
    `p`.`barcode_ean_13` as `barcode_ean_13`,
    `p`.`barcode_ean_128` as `barcode_ean_128`,
    `p`.`articolo` as `articolo`,
    group_concat(distinct `p`.`ingrediente_descrizione` order by `p`.`percentuale` desc, `p`.`descrizione` asc separator ',') as `ingredienti`,
    p.tracce as ingredienti_2,
    `p`.`valori_nutrizionali` as `valori_nutrizionali`,
    `p`.`conservazione` as `conservazione`
from
    `v_produzione_etichetta_sub` `p`
group by
    `p`.`id`,
    `p`.`lotto`,
    `p`.`scadenza`,
    `p`.`barcode_ean_13`,
    `p`.`barcode_ean_128`,
    `p`.`articolo`,
    `p`.`valori_nutrizionali`,
    `p`.`conservazione`,
    p.tracce;
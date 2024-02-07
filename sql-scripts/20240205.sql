drop view if exists contarbn.v_ingrediente;

ALTER TABLE contarbn.ingrediente ADD composto bit(1) NOT NULL DEFAULT b'0' after data_inserimento;
ALTER TABLE contarbn.ingrediente ADD composizione text after composto;

CREATE TABLE contarbn.ingrediente_allergene_composizione (
    `id_ingrediente` int unsigned NOT NULL,
    `id_allergene` int unsigned NOT NULL,
    PRIMARY KEY (`id_ingrediente`,`id_allergene`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

-- VIEWS
create or replace
algorithm = UNDEFINED view contarbn.`v_ricetta` as
select
    `ricetta`.`id` as `id_ricetta`,
    `ricetta`.`nome` as `nome_ricetta`,
    `ricetta`.`tempo_preparazione` as `tempo_preparazione`,
    `ricetta`.`peso_totale` as `peso_totale`,
    `ricetta`.`valori_nutrizionali` as `valori_nutrizionali`,
    `ricetta`.`conservazione` as `conservazione`,
    group_concat(distinct `allergene`.`nome` order by `allergene`.`nome` desc separator ',') as `tracce`
from
    ((`ricetta`
        left join `ricetta_allergene` on
            ((`ricetta_allergene`.`id_ricetta` = `ricetta`.`id`)))
        left join `allergene` on
        ((`ricetta_allergene`.`id_allergene` = `allergene`.`id`)))
group by
    `ricetta`.`id`,
    `ricetta`.`nome`,
    `ricetta`.`tempo_preparazione`,
    `ricetta`.`peso_totale`,
    `ricetta`.`valori_nutrizionali`,
    `ricetta`.`conservazione`;

create or replace view contarbn.v_ricetta_allergene as
select
    ricetta_allergene.id_ricetta,
    ricetta_allergene.id_allergene
from contarbn.ricetta_allergene
union
select
    ricetta_ingrediente.id_ricetta,
    ingrediente_allergene_composizione.id_allergene
from contarbn.ricetta_ingrediente
         join contarbn.ingrediente_allergene_composizione on
        ricetta_ingrediente.id_ingrediente = ingrediente_allergene_composizione.id_ingrediente
;

create or replace algorithm = UNDEFINED view contarbn.v_ingrediente as
select
    ingrediente.id,
    ingrediente.codice,
    ingrediente.descrizione,
    ingrediente.prezzo,
    ingrediente.id_unita_misura,
    ingrediente.id_fornitore,
    case
        when fornitore.ragione_sociale is not null then
            fornitore.ragione_sociale
        else
            fornitore.ragione_sociale_2
        end as fornitore,
    ingrediente.id_aliquota_iva,
    ingrediente.scadenza_giorni,
    ingrediente.data_inserimento,
    ingrediente.composto,
    ingrediente.composizione,
    ingrediente.attivo,
    ingrediente.note,
    case
        when ingrediente.composto = 0 then
            ingrediente.descrizione
        else
            concat(ingrediente.descrizione,'(',replace(replace(ingrediente.composizione,'<p>',''),'</p>',''),')')
        end as descrizione_scheda_tecnica
FROM contarbn.ingrediente
         join contarbn.fornitore on
        fornitore.id = ingrediente.id_fornitore
;

create or replace
algorithm = UNDEFINED view `v_ricetta_info` as
select
    `ricetta`.`id` as `id`,
    `ricetta`.`nome` as `nome`,
    `ricetta`.`tempo_preparazione` as `tempo_preparazione`,
    `ricetta`.`peso_totale` as `peso_totale`,
    `ricetta`.`valori_nutrizionali` as `valori_nutrizionali`,
    `ricetta`.`conservazione` as `conservazione`,
    `ricetta`.`consigli_consumo` as `consigli_consumo`,
    `ricetta`.`scadenza_giorni` as `scadenza_giorni`,
    group_concat(distinct v_ingrediente.descrizione_scheda_tecnica order by ricetta_ingrediente.quantita desc separator ', ') as `ingredienti`,
    group_concat(distinct `allergene`.`nome` order by `allergene`.`ordine` asc separator ', ') as `allergeni_tracce`
from
    `ricetta`
        join `ricetta_ingrediente` on
            `ricetta_ingrediente`.`id_ricetta` = `ricetta`.`id`
        join v_ingrediente on
            v_ingrediente.id = `ricetta_ingrediente`.`id_ingrediente`
        left join `v_ricetta_allergene` on
            `v_ricetta_allergene`.`id_ricetta` = `ricetta`.`id`
        left join `allergene` on
            `v_ricetta_allergene`.`id_allergene` = `allergene`.`id`
group by
    `ricetta`.`id`,
    `ricetta`.`nome`,
    `ricetta`.`tempo_preparazione`,
    `ricetta`.`peso_totale`,
    `ricetta`.`valori_nutrizionali`,
    `ricetta`.`conservazione`,
    `ricetta`.`consigli_consumo`,
    `ricetta`.`scadenza_giorni`;
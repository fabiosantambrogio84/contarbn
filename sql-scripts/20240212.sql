CREATE TABLE contarbn.ricetta_20240215
select *
from contarbn.ricetta;

CREATE TABLE contarbn.`ricetta_analisi` (
    `id_ricetta` int unsigned NOT NULL,
    `id_analisi` int unsigned NOT NULL,
    `risultato` varchar(255) COLLATE latin1_general_cs DEFAULT NULL,
    PRIMARY KEY (`id_ricetta`,`id_analisi`),
    CONSTRAINT `fk_ricetta_analisi_analisi` FOREIGN KEY (`id_analisi`) REFERENCES `anagrafica` (`id`),
    CONSTRAINT `fk_ricetta_analisi_ricetta` FOREIGN KEY (`id_ricetta`) REFERENCES `ricetta` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE contarbn.`ricetta_nutriente` (
    `id_ricetta` int unsigned NOT NULL,
    `id_nutriente` int unsigned NOT NULL,
    `valore` varchar(255) COLLATE latin1_general_cs DEFAULT NULL,
    PRIMARY KEY (`id_ricetta`,`id_nutriente`),
    CONSTRAINT `fk_ricetta_nutriente_nutriente` FOREIGN KEY (`id_nutriente`) REFERENCES `anagrafica` (`id`),
    CONSTRAINT `fk_ricetta_nutriente_scheda_tecnica` FOREIGN KEY (`id_ricetta`) REFERENCES `ricetta` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

alter table contarbn.ricetta drop column allergeni;
alter table contarbn.ricetta drop column valori_nutrizionali;

-- VIEWS

create or replace algorithm = UNDEFINED view `v_ricetta` as
select
    `ricetta`.`id` as `id_ricetta`,
    `ricetta`.`nome` as `nome_ricetta`,
    `ricetta`.`tempo_preparazione` as `tempo_preparazione`,
    `ricetta`.`peso_totale` as `peso_totale`,
    group_concat(distinct concat(anagrafica.`nome`,' ',ricetta_nutriente.valore) order by anagrafica.ordine separator ',') as valori_nutrizionali,
    `ricetta`.`conservazione` as `conservazione`,
    group_concat(distinct `allergene`.`nome` order by `allergene`.`nome` desc separator ',') as `tracce`
from
    `ricetta`
        left join ricetta_nutriente on
            ricetta_nutriente.id_ricetta = ricetta.id
        left join anagrafica on
            ricetta_nutriente.id_nutriente = anagrafica.id
        left join `ricetta_allergene` on
            `ricetta_allergene`.`id_ricetta` = `ricetta`.`id`
        left join `allergene` on
            `ricetta_allergene`.`id_allergene` = `allergene`.`id`
group by
    `ricetta`.`id`,
    `ricetta`.`nome`,
    `ricetta`.`tempo_preparazione`,
    `ricetta`.`peso_totale`,
    `ricetta`.`conservazione`;

create or replace
algorithm = UNDEFINED view `v_ricetta_info` as
select
    `ricetta`.`id` as `id`,
    `ricetta`.`nome` as `nome`,
    `ricetta`.`tempo_preparazione` as `tempo_preparazione`,
    `ricetta`.`peso_totale` as `peso_totale`,
    `ricetta`.`conservazione` as `conservazione`,
    `ricetta`.`consigli_consumo` as `consigli_consumo`,
    `ricetta`.`scadenza_giorni` as `scadenza_giorni`,
    group_concat(distinct `v_ingrediente`.`descrizione_scheda_tecnica` order by `ricetta_ingrediente`.`quantita` desc separator ', ') as `ingredienti`,
    group_concat(distinct `allergene`.`nome` order by `allergene`.`ordine` asc separator ', ') as `allergeni_tracce`
from
    ((((`ricetta`
        join `ricetta_ingrediente` on
            ((`ricetta_ingrediente`.`id_ricetta` = `ricetta`.`id`)))
        join `v_ingrediente` on
            ((`v_ingrediente`.`id` = `ricetta_ingrediente`.`id_ingrediente`)))
        left join `v_ricetta_allergene` on
            ((`v_ricetta_allergene`.`id_ricetta` = `ricetta`.`id`)))
        left join `allergene` on
        ((`v_ricetta_allergene`.`id_allergene` = `allergene`.`id`)))
group by
    `ricetta`.`id`,
    `ricetta`.`nome`,
    `ricetta`.`tempo_preparazione`,
    `ricetta`.`peso_totale`,
    `ricetta`.`conservazione`,
    `ricetta`.`consigli_consumo`,
    `ricetta`.`scadenza_giorni`;


create or replace algorithm = UNDEFINED view `v_scheda_tecnica` as
select
    uuid() as `id`,
    `scheda_tecnica`.`id` as `id_scheda_tecnica`,
    `produzione_confezione`.`id_produzione` as `id_produzione`,
    `produzione_confezione`.`id_articolo` as `id_articolo`,
    `v_ricetta_info`.`id` as `id_ricetta`,
    `scheda_tecnica`.`num_revisione` as `num_revisione`,
    coalesce(`scheda_tecnica`.`anno`, year(curdate())) as `anno`,
    coalesce(`scheda_tecnica`.`data`, curdate()) as `data`,
    coalesce(`scheda_tecnica`.`codice_prodotto`, `articolo`.`codice`) as `codice_prodotto`,
    coalesce(`scheda_tecnica`.`prodotto`, `articolo`.`descrizione`) as `prodotto`,
    coalesce(`scheda_tecnica`.`prodotto_2`, `articolo`.`descrizione_2`) as `prodotto_2`,
    coalesce(`scheda_tecnica`.`peso_netto_confezione`, `confezione`.`tipo`) as `peso_netto_confezione`,
    coalesce(`scheda_tecnica`.`ingredienti`, concat(upper(left(`v_ricetta_info`.`ingredienti`, 1)), lower(substr(`v_ricetta_info`.`ingredienti`, 2)))) as `ingredienti`,
    coalesce(`scheda_tecnica`.`tracce`, `v_ricetta_info`.`allergeni_tracce`) as `tracce`,
    coalesce(`scheda_tecnica`.`durata`, `articolo`.`scadenza_giorni`) as `durata`,
    coalesce(`scheda_tecnica`.`conservazione`, `v_ricetta_info`.`conservazione`) as `conservazione`,
    coalesce(`scheda_tecnica`.`consigli_consumo`, `v_ricetta_info`.`consigli_consumo`) as `consigli_consumo`,
    `scheda_tecnica`.`id_tipologia_confezionamento` as `id_tipologia_confezionamento`,
    `tipologia_confezionamento`.`nome` as `tipologia_confezionamento`,
    `scheda_tecnica`.`id_imballo` as `id_imballo`,
    `imballo`.`nome` as `imballo`,
    `scheda_tecnica`.`imballo_dimensioni` as `imballo_dimensioni`
from
    (((((((`produzione`
        join `produzione_confezione` on
            ((`produzione`.`id` = `produzione_confezione`.`id_produzione`)))
        join `confezione` on
            ((`produzione_confezione`.`id_confezione` = `confezione`.`id`)))
        join `articolo` on
            ((`produzione_confezione`.`id_articolo` = `articolo`.`id`)))
        join `v_ricetta_info` on
            ((`produzione`.`id_ricetta` = `v_ricetta_info`.`id`)))
        left join `scheda_tecnica` on
            (((`produzione_confezione`.`id_produzione` = `scheda_tecnica`.`id_produzione`)
                and (`produzione_confezione`.`id_articolo` = `scheda_tecnica`.`id_articolo`))))
        left join `anagrafica` `tipologia_confezionamento` on
            ((`scheda_tecnica`.`id_tipologia_confezionamento` = `tipologia_confezionamento`.`id`)))
        left join `anagrafica` `imballo` on
        ((`scheda_tecnica`.`id_imballo` = `imballo`.`id`)))
where
    (`produzione`.`tipologia` = 'STANDARD');
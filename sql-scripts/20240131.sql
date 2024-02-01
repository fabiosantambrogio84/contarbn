drop view if exists contarbn.v_scheda_tecnica;
drop view if exists contarbn.v_ricetta_info;

ALTER TABLE contarbn.articolo ADD descrizione_2 varchar(255) DEFAULT NULL after descrizione;

ALTER TABLE contarbn.ricetta DROP COLUMN nome_2;
ALTER TABLE contarbn.ricetta ADD consigli_consumo text DEFAULT NULL after conservazione;

ALTER TABLE contarbn.allergene ADD ordine int DEFAULT NULL after nome;
ALTER TABLE contarbn.allergene ADD attivo bit(1) NOT NULL DEFAULT b'1' after ordine;
UPDATE contarbn.allergene SET ordine=id, attivo=1;

TRUNCATE TABLE contarbn.scheda_tecnica_analisi;
TRUNCATE TABLE contarbn.scheda_tecnica_nutriente;
TRUNCATE TABLE contarbn.scheda_tecnica_raccolta;
delete FROM contarbn.scheda_tecnica;
ALTER TABLE contarbn.scheda_tecnica DROP COLUMN id_ricetta;
--ALTER TABLE contarbn.scheda_tecnica RENAME COLUMN allergeni_tracce TO tracce;
ALTER TABLE `contarbn`.`scheda_tecnica` CHANGE COLUMN `allergeni_tracce` `tracce` VARCHAR(255) CHARACTER SET 'latin1' COLLATE 'latin1_general_cs' NULL DEFAULT NULL ;

ALTER TABLE contarbn.scheda_tecnica ADD id_produzione int unsigned DEFAULT NULL after id;
ALTER TABLE contarbn.scheda_tecnica ADD id_articolo int unsigned DEFAULT NULL after id_produzione;
ALTER TABLE contarbn.scheda_tecnica ADD codice_prodotto varchar(100) DEFAULT NULL after data;

-- VIEWS
create or replace algorithm = UNDEFINED view `v_ricetta_info` as
select
    ricetta.id,
    ricetta.nome,
    ricetta.tempo_preparazione,
    ricetta.peso_totale,
    ricetta.valori_nutrizionali,
    ricetta.conservazione,
    ricetta.consigli_consumo,
    ricetta.scadenza_giorni,
    group_concat(distinct ingrediente.descrizione order by ingrediente.descrizione desc separator ', ') as ingredienti,
    group_concat(distinct `allergene`.`nome` order by `allergene`.`ordine` separator ', ') as allergeni_tracce
from
    contarbn.ricetta
        join contarbn.ricetta_ingrediente on
            ricetta_ingrediente.id_ricetta = ricetta.id
        join ingrediente on
            ingrediente.id = ricetta_ingrediente.id_ingrediente
        left join ricetta_allergene on
            ricetta_allergene.id_ricetta = ricetta_ingrediente.id_ricetta
        left join allergene on
            ricetta_allergene.id_allergene = allergene.id
group by
    ricetta.id,
    ricetta.nome,
    ricetta.tempo_preparazione,
    ricetta.peso_totale,
    ricetta.valori_nutrizionali,
    ricetta.conservazione,
    ricetta.consigli_consumo,
    ricetta.scadenza_giorni
;

create or replace algorithm = UNDEFINED view `v_scheda_tecnica` as
select
    uuid() as id,
    scheda_tecnica.id as id_scheda_tecnica,
    produzione_confezione.id_produzione,
    produzione_confezione.id_articolo,
    v_ricetta_info.id as id_ricetta,
    -- articolo.codice as codice_articolo,
    -- articolo.descrizione as descrizione_articolo,
    -- articolo.descrizione_2 as descrizione_2_articolo,
    scheda_tecnica.num_revisione as num_revisione,
    coalesce(scheda_tecnica.anno, year(current_date())) as anno,
    coalesce(scheda_tecnica.data, current_date()) as data,
    coalesce(scheda_tecnica.codice_prodotto, articolo.codice) as codice_prodotto,
    coalesce(scheda_tecnica.prodotto, articolo.descrizione) as prodotto,
    coalesce(scheda_tecnica.prodotto_2, articolo.descrizione_2) as prodotto_2,
    coalesce(scheda_tecnica.peso_netto_confezione, v_ricetta_info.peso_totale) as peso_netto_confezione,
    coalesce(scheda_tecnica.ingredienti, CONCAT(UCASE(LEFT(v_ricetta_info.ingredienti, 1)), LCASE(SUBSTRING(v_ricetta_info.ingredienti, 2)))) as ingredienti,
    coalesce(scheda_tecnica.tracce, v_ricetta_info.allergeni_tracce) as tracce,
    coalesce(scheda_tecnica.durata, v_ricetta_info.tempo_preparazione) as durata,
    coalesce(scheda_tecnica.conservazione, v_ricetta_info.conservazione) as conservazione,
    coalesce(scheda_tecnica.consigli_consumo, v_ricetta_info.consigli_consumo) as consigli_consumo,
    scheda_tecnica.id_tipologia_confezionamento,
    tipologia_confezionamento.nome as tipologia_confezionamento,
    scheda_tecnica.id_imballo,
    imballo.nome as imballo,
    scheda_tecnica.imballo_dimensioni
from
    contarbn.produzione
        join contarbn.produzione_confezione on
            produzione.id = produzione_confezione.id_produzione
        join contarbn.confezione on
            produzione_confezione.id_confezione = confezione.id
        join contarbn.articolo on
            produzione_confezione.id_articolo = articolo.id
        join contarbn.v_ricetta_info on
            produzione.id_ricetta =	v_ricetta_info.id
        left join contarbn.scheda_tecnica on
                produzione_confezione.id_produzione = scheda_tecnica.id_produzione and
                produzione_confezione.id_articolo = scheda_tecnica.id_articolo
        left join contarbn.anagrafica tipologia_confezionamento on
            scheda_tecnica.id_tipologia_confezionamento = tipologia_confezionamento.id
        left join contarbn.anagrafica imballo on
            scheda_tecnica.id_imballo = imballo.id
where
        produzione.tipologia = 'STANDARD'
;
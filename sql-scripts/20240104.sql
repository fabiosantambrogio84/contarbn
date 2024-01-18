drop view if exists contarbn.v_scheda_tecnica;
drop view if exists contarbn.v_ricetta_info;
drop view if exists contarbn.v_ricetta;

drop table if exists contarbn.scheda_tecnica_raccolta;
drop table if exists contarbn.scheda_tecnica_analisi;
drop table if exists contarbn.scheda_tecnica_nutriente;
drop table if exists contarbn.scheda_tecnica;
drop table if exists contarbn.anagrafica;

CREATE TABLE contarbn.anagrafica (
    id int unsigned NOT NULL AUTO_INCREMENT,
    tipo varchar(255),
    nome varchar(255),
    descrizione varchar(255),
    `attivo` bit(1) NOT NULL DEFAULT b'1',
    `data_inserimento` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `data_aggiornamento` timestamp NULL DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

truncate table contarbn.anagrafica;

INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('MATERIALE', 'Vaschetta  PP', '', 1, CURRENT_TIMESTAMP);
INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('MATERIALE', 'Pellicola 7 >PET+PP<', '', 1, CURRENT_TIMESTAMP);
INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('RACCOLTA_DIFFERENZIATA', 'Plastica', '', 1, CURRENT_TIMESTAMP);
INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('TIPOLOGIA_CONFEZIONAMENTO', 'Termosaldatura', '', 1, CURRENT_TIMESTAMP);
INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('IMBALLO', 'PP - PET (Vaschetta - Pellicola)', '', 1, CURRENT_TIMESTAMP);
INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('NUTRIENTE', 'Energia', '', 1, CURRENT_TIMESTAMP);
INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('NUTRIENTE', 'Grassi (di cui saturi)', '', 1, CURRENT_TIMESTAMP);
INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('NUTRIENTE', 'Carboidrati (di cui zuccheri)', '', 1, CURRENT_TIMESTAMP);
INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('NUTRIENTE', 'Proteine', '', 1, CURRENT_TIMESTAMP);
INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('NUTRIENTE', 'Sale', '', 1, CURRENT_TIMESTAMP);
INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('ANALISI_MICROBIOLOGICA', 'CMT', '', 1, CURRENT_TIMESTAMP);
INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('ANALISI_MICROBIOLOGICA', 'E.coli', '', 1, CURRENT_TIMESTAMP);
INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('ANALISI_MICROBIOLOGICA', 'Clostridi solfito riduttori', '', 1, CURRENT_TIMESTAMP);
INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('ANALISI_MICROBIOLOGICA', 'Staphylococcus aureus', '', 1, CURRENT_TIMESTAMP);
INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('ANALISI_MICROBIOLOGICA', 'Salmonella spp', '', 1, CURRENT_TIMESTAMP);
INSERT INTO contarbn.anagrafica(tipo, nome, descrizione, attivo, data_inserimento) VALUES('ANALISI_MICROBIOLOGICA', 'Listeria Monocytogenes', '', 1, CURRENT_TIMESTAMP);


CREATE TABLE contarbn.`scheda_tecnica` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  id_ricetta int unsigned,
  num_revisione int default 1,
  anno int,
  `data` date DEFAULT NULL,
  `prodotto` varchar(255) DEFAULT NULL,
  `prodotto_2` varchar(255) DEFAULT NULL,
  `peso_netto_confezione` varchar(255) DEFAULT NULL,
  `ingredienti` varchar(255) DEFAULT NULL,
  `allergeni_tracce` varchar(255) DEFAULT NULL,
  `durata` varchar(255) DEFAULT NULL,
  `conservazione` varchar(255) DEFAULT NULL,
  `consigli_consumo` varchar(255) DEFAULT NULL,
  `id_tipologia_confezionamento` int unsigned,
  `id_imballo` int unsigned,
  `imballo_dimensioni` varchar(255) DEFAULT NULL,
  `data_inserimento` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `data_aggiornamento` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_scheda_tecnica_tipo_confezionamento` FOREIGN KEY (`id_tipologia_confezionamento`) REFERENCES `anagrafica` (`id`),
  CONSTRAINT `fk_scheda_tecnica_imballo` FOREIGN KEY (`id_imballo`) REFERENCES `anagrafica` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE contarbn.`scheda_tecnica_nutriente` (
   id_scheda_tecnica int unsigned NOT NULL,
   id_nutriente int unsigned NOT NULL,
   `valore` varchar(255) DEFAULT NULL,
   PRIMARY KEY (`id_scheda_tecnica`, id_nutriente),
   CONSTRAINT `fk_scheda_tecnica_nutriente_scheda_tecnica` FOREIGN KEY (`id_scheda_tecnica`) REFERENCES `scheda_tecnica` (`id`),
   CONSTRAINT `fk_scheda_tecnica_nutriente_nutriente` FOREIGN KEY (`id_nutriente`) REFERENCES `anagrafica` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE contarbn.`scheda_tecnica_analisi` (
     id_scheda_tecnica int unsigned NOT NULL,
     id_analisi int unsigned NOT NULL,
    `risultato` varchar(255) DEFAULT NULL,
     PRIMARY KEY (`id_scheda_tecnica`, id_analisi),
     CONSTRAINT `fk_scheda_tecnica_analisi_scheda_tecnica` FOREIGN KEY (`id_scheda_tecnica`) REFERENCES `scheda_tecnica` (`id`),
     CONSTRAINT `fk_scheda_tecnica_analisi_analisi` FOREIGN KEY (`id_analisi`) REFERENCES `anagrafica` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE contarbn.`scheda_tecnica_raccolta` (
    id_scheda_tecnica int unsigned NOT NULL,
    id_materiale int unsigned NOT NULL,
    id_raccolta int unsigned NOT NULL,
    PRIMARY KEY (`id_scheda_tecnica`, id_materiale),
    CONSTRAINT `fk_scheda_tecnica_raccolta_scheda_tecnica` FOREIGN KEY (`id_scheda_tecnica`) REFERENCES `scheda_tecnica` (`id`),
    CONSTRAINT `fk_scheda_tecnica_raccolta_materiale` FOREIGN KEY (`id_materiale`) REFERENCES `anagrafica` (`id`),
    CONSTRAINT `fk_scheda_tecnica_raccolta_raccolta` FOREIGN KEY (`id_raccolta`) REFERENCES `anagrafica` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;


ALTER TABLE contarbn.ricetta ADD nome_2 varchar(100) DEFAULT NULL after nome;

create or replace algorithm = UNDEFINED view `v_ricetta` as
select
    `ricetta`.`id` as `id_ricetta`,
    `ricetta`.`nome` as `nome_ricetta`,
    `ricetta`.`nome_2` as `nome_ricetta_2`,
    ricetta.tempo_preparazione,
    ricetta.peso_totale,
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
    ricetta.nome_2,
    ricetta.tempo_preparazione,
    ricetta.peso_totale,
    `ricetta`.`valori_nutrizionali`,
    `ricetta`.`conservazione`;

create or replace algorithm = UNDEFINED view `v_ricetta_info` as
select
    ricetta.id,
    ricetta.nome,
    ricetta.nome_2,
    ricetta.tempo_preparazione,
    ricetta.peso_totale,
    ricetta.valori_nutrizionali,
    ricetta.conservazione,
    group_concat(distinct ingrediente.descrizione order by ingrediente.descrizione desc separator ', ') as ingredienti,
    group_concat(distinct `allergene`.`nome` order by `allergene`.`nome` desc separator ', ') as allergeni_tracce
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
    ricetta.nome_2,
    ricetta.tempo_preparazione,
    ricetta.peso_totale,
    ricetta.valori_nutrizionali,
    ricetta.conservazione
;

create or replace algorithm = UNDEFINED view `v_scheda_tecnica` as
select
    uuid() as id,
    v_ricetta_info.id as id_ricetta,
    scheda_tecnica.id as id_scheda_tecnica,
    scheda_tecnica.num_revisione as num_revisione,
    coalesce(scheda_tecnica.anno, year(current_date())) as anno,
    coalesce(scheda_tecnica.data, current_date()) as data,
    coalesce(scheda_tecnica.prodotto, v_ricetta_info.nome) as prodotto,
    coalesce(scheda_tecnica.prodotto_2, v_ricetta_info.nome_2) as prodotto_2,
    coalesce(scheda_tecnica.peso_netto_confezione, v_ricetta_info.peso_totale) as peso_netto_confezione,
    coalesce(scheda_tecnica.ingredienti, CONCAT(UCASE(LEFT(v_ricetta_info.ingredienti, 1)), LCASE(SUBSTRING(v_ricetta_info.ingredienti, 2)))) as ingredienti,
    coalesce(scheda_tecnica.allergeni_tracce, v_ricetta_info.allergeni_tracce) as allergeni_tracce,
    coalesce(scheda_tecnica.durata, v_ricetta_info.tempo_preparazione) as durata,
    coalesce(scheda_tecnica.conservazione, v_ricetta_info.conservazione) as conservazione,
    scheda_tecnica.consigli_consumo,
    scheda_tecnica.id_tipologia_confezionamento,
    tipologia_confezionamento.nome as tipologia_confezionamento,
    scheda_tecnica.id_imballo,
    imballo.nome as imballo,
    scheda_tecnica.imballo_dimensioni
from
    contarbn.v_ricetta_info
        left join contarbn.scheda_tecnica on
            v_ricetta_info.id = scheda_tecnica.id_ricetta
        left join contarbn.anagrafica tipologia_confezionamento on
            scheda_tecnica.id_tipologia_confezionamento = tipologia_confezionamento.id
        left join contarbn.anagrafica imballo on
            scheda_tecnica.id_imballo = imballo.id
;

--
ALTER TABLE contarbn.ingrediente ADD scadenza_giorni int DEFAULT NULL after id_aliquota_iva;

create or replace view contarbn.v_giacenza_articolo as
select
    giacenza_articolo.id,
    giacenza_articolo.id_articolo,
    concat(articolo.codice, ' ', coalesce(articolo.descrizione, '')) as `articolo`,
    articolo.prezzo_acquisto,
    articolo.prezzo_listino_base,
    giacenza_articolo.quantita,
    articolo.attivo,
    articolo.id_fornitore,
    fornitore.ragione_sociale as fornitore,
    giacenza_articolo.lotto,
    giacenza_articolo.scadenza,
    articolo.scadenza_giorni,
    case
        when current_date >= (giacenza_articolo.scadenza - interval coalesce(articolo.scadenza_giorni,0) DAY) then
            1
        else
            0
        end as scaduto
from
    contarbn.giacenza_articolo
        join articolo on
            giacenza_articolo.id_articolo = articolo.id
        left join fornitore on
            articolo.id_fornitore = fornitore.id
;

create or replace view contarbn.v_giacenza_articolo_agg as
select
    v_giacenza_articolo.id_articolo,
    v_giacenza_articolo.articolo,
    v_giacenza_articolo.prezzo_acquisto,
    v_giacenza_articolo.prezzo_listino_base,
    v_giacenza_articolo.attivo,
    v_giacenza_articolo.id_fornitore,
    v_giacenza_articolo.fornitore,
    sum(v_giacenza_articolo.quantita) as quantita_tot,
    null as quantita_kg,
    case
        when sum(v_giacenza_articolo.scaduto) > 0 then
            1
        else
            0
        end as scaduto
from
    contarbn.v_giacenza_articolo
group by
    v_giacenza_articolo.id_articolo,
    v_giacenza_articolo.articolo,
    v_giacenza_articolo.prezzo_acquisto,
    v_giacenza_articolo.prezzo_listino_base,
    v_giacenza_articolo.attivo,
    v_giacenza_articolo.id_fornitore,
    v_giacenza_articolo.fornitore
;

create or replace view contarbn.v_giacenza_ingrediente as
select
    giacenza_ingrediente.id,
    giacenza_ingrediente.id_ingrediente,
    concat(ingrediente.codice, ' ', coalesce(ingrediente.descrizione, '')) as ingrediente,
    giacenza_ingrediente.quantita,
    ingrediente.attivo,
    unita_misura.etichetta as udm,
    ingrediente.id_fornitore,
    fornitore.ragione_sociale as fornitore,
    ingrediente.codice as codice_ingrediente,
    ingrediente.descrizione as descrizione_ingrediente,
    giacenza_ingrediente.lotto,
    giacenza_ingrediente.scadenza,
    ingrediente.scadenza_giorni,
    (case
         when (curdate() >= (giacenza_ingrediente.scadenza - interval coalesce(ingrediente.scadenza_giorni, 0) day)) then 1
         else 0
        end) as scaduto
from
    contarbn.giacenza_ingrediente
        join ingrediente on
            giacenza_ingrediente.id_ingrediente = ingrediente.id
        left join unita_misura on
            ingrediente.id_unita_misura = unita_misura.id
        left join fornitore on
            ingrediente.id_fornitore = fornitore.id
;

create or replace view contarbn.v_giacenza_ingrediente_agg as
select
    v_giacenza_ingrediente.id_ingrediente,
    v_giacenza_ingrediente.ingrediente,
    sum(v_giacenza_ingrediente.quantita) as `quantita_tot`,
    v_giacenza_ingrediente.attivo,
    v_giacenza_ingrediente.udm,
    v_giacenza_ingrediente.id_fornitore,
    v_giacenza_ingrediente.fornitore,
    v_giacenza_ingrediente.codice_ingrediente,
    v_giacenza_ingrediente.descrizione_ingrediente,
    (case
         when (sum(v_giacenza_ingrediente.scaduto) > 0) then 1
         else 0
        end) as `scaduto`
from
    v_giacenza_ingrediente
group by
    v_giacenza_ingrediente.id_ingrediente,
    v_giacenza_ingrediente.ingrediente,
    v_giacenza_ingrediente.attivo,
    v_giacenza_ingrediente.udm,
    v_giacenza_ingrediente.id_fornitore,
    v_giacenza_ingrediente.fornitore,
    v_giacenza_ingrediente.codice_ingrediente,
    v_giacenza_ingrediente.descrizione_ingrediente
;

--
UPDATE contarbn.articolo SET scadenza_giorni=5 WHERE scadenza_giorni IS NULL;

UPDATE contarbn.ingrediente SET scadenza_giorni=5 WHERE scadenza_giorni IS NULL;
CREATE TABLE contarbn.`ditta_info` (
    `id` int unsigned NOT NULL AUTO_INCREMENT,
    `codice` varchar(100) DEFAULT NULL,
    `dato` varchar(100) DEFAULT NULL,
    `descrizione` varchar(255) DEFAULT NULL,
    `valore` varchar(255) DEFAULT NULL,
    `deletable` bit(1) NOT NULL DEFAULT b'0',
    `data_inserimento` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `data_aggiornamento` timestamp NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(1, 'RAGIONE_SOCIALE', 'Ragione sociale', 'Ragione sociale', 'Urbani alimentari SNC', b'0', now(), now());

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(2, 'INDIRIZZO', 'Indirizzo', 'Indirizzo', 'Via 11 Settembre, 17', b'0', now(), now());

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(3, 'CITTA', 'Citta', 'Citta', 'San Giovanni Ilarione', b'0', now(), now());

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(4, 'PROVINCIA', 'Provincia', 'Provincia', 'Verona', b'0', now(), now());

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(5, 'CAP', 'Cap', 'Cap', '37035', b'0', now(), now());

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(6, 'PARTITA_IVA', 'Partita iva', 'Partita iva', '04998580239', b'0', now(), now());

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(7, 'CODICE_FISCALE', 'Codice fiscale', 'Codice fiscale', '04998580239', b'0', now(), now());

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(8, 'REA', 'Rea', 'Rea', 'VR462414', b'0', now(), now());

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(9, 'TELEFONO', 'Telefono', 'Telefono', '045 6550993', b'0', now(), now());

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(10, 'CELLULARE', 'Cellulare', 'Cellulare', '328 4694654', b'0', now(), now());

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(11, 'WEBSITE', 'Website', 'Website', 'www.urbanialimentari.com', b'0', now(), now());

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(12, 'EMAIL', 'Email', 'Email', 'info@urbanialimentari.com', b'0', now(), now());

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(13, 'PEC', 'Pec', 'Pec', 'rbn.snc@legalmail.it', b'0', now(), now());

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(14, 'REPORT_INTESTAZIONE', 'Report intestazione', 'Report intestazione', 'URBANI ALIMENTARI SNC di Urbani Elia e Marta', b'0', now(), now());

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(15, 'REPORT_INTESTAZIONE_2', 'Report intestazione 2', 'Report intestazione 2', 'gastronomia – pasta fresca', b'0', now(), now());

INSERT INTO contarbn.ditta_info (id, codice, dato, descrizione, valore, deletable, data_inserimento, data_aggiornamento)
VALUES(16, 'REPORT_INDIRIZZO', 'Report indirizzo', 'Report indirizzo', '37035 SAN GIOVANNI ILARIONE / VR - Via 11 Settembre, 17', b'0', now(), now());

-------------------------------------------
alter table contarbn.ricetta add conservazione text NULL after valori_nutrizionali;

create or replace
algorithm = UNDEFINED view contarbn.`v_produzione_etichetta` as
select
    `produzione`.`id` as `id`,
    `produzione`.`lotto` as `lotto`,
    `produzione`.`scadenza` as `scadenza`,
    `ricetta`.`nome` as `articolo`,
    group_concat(distinct `ingrediente`.`descrizione` order by `ingrediente`.`descrizione` asc separator ',') as `ingredienti`,
    ricetta.valori_nutrizionali,
    ricetta.conservazione
from
    (((`produzione`
        join `ricetta` on
            ((`produzione`.`id_ricetta` = `ricetta`.`id`)))
        left join `produzione_ingrediente` on
            ((`produzione`.`id` = `produzione_ingrediente`.`id_produzione`)))
        left join `ingrediente` on
        ((`produzione_ingrediente`.`id_ingrediente` = `ingrediente`.`id`)))
group by
    `produzione`.`id`,
    `produzione`.`lotto`,
    `produzione`.`scadenza`,
    `ricetta`.`nome`,
    ricetta.valori_nutrizionali,
    ricetta.conservazione
;

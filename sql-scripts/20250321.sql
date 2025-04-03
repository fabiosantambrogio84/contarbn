ALTER TABLE contarbn.ddt MODIFY COLUMN note text CHARACTER SET latin1 COLLATE latin1_general_cs NULL;


ALTER TABLE contarbn.ddt ADD consegnato bit(1) NOT NULL DEFAULT b'0' after note;
ALTER TABLE contarbn.fattura_accom ADD consegnato bit(1) NOT NULL DEFAULT b'0' after note;
ALTER TABLE contarbn.ricevuta_privato ADD consegnato bit(1) NOT NULL DEFAULT b'0' after note;
ALTER TABLE contarbn.nota_reso ADD consegnato bit(1) NOT NULL DEFAULT b'0' after note;
alter table contarbn.nota_reso add column tipo_trasporto varchar(100) after spedito_ade;
ALTER TABLE contarbn.nota_reso add column data_trasporto DATE after tipo_trasporto;
ALTER TABLE contarbn.nota_reso add column ora_trasporto TIME after data_trasporto;
ALTER TABLE contarbn.nota_reso add column id_trasportatore int unsigned after ora_trasporto;
ALTER TABLE contarbn.nota_reso ADD CONSTRAINT `fk_nota_reso_trasportatore` FOREIGN KEY (`id_trasportatore`) REFERENCES `trasportatore` (`id`);

-- update contarbn.ddt set consegnato=1 where data <= now();
-- update contarbn.fattura_accom set consegnato=1 where data <= now();
-- update contarbn.ricevuta_privato set consegnato=1 where data <= now();
-- update contarbn.nota_reso set consegnato=1 where data <= now();

drop view if exists contarbn.v_bordero_riga;
drop table if exists contarbn.bordero_detail;
drop table if exists contarbn.bordero_riga;
drop table if exists contarbn.bordero;

CREATE TABLE contarbn.bordero (
    id int unsigned NOT NULL AUTO_INCREMENT,
    id_autista int unsigned,
    id_trasportatore int unsigned,
    data_consegna date,
    data_inserimento timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT `fk_bordero_autista` FOREIGN KEY (`id_autista`) REFERENCES `autista` (`id`),
    CONSTRAINT `fk_bordero_trasportatore` FOREIGN KEY (`id_trasportatore`) REFERENCES `trasportatore` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE contarbn.bordero_detail (
    id int unsigned NOT NULL AUTO_INCREMENT,
    id_bordero int unsigned,
    id_cliente int unsigned,
    id_punto_consegna int unsigned,
    id_fornitore int unsigned,
    tipo_documento varchar(255),
    id_documento int unsigned,
    data_consegna date,
    note text,
    data_inserimento timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT `fk_bordero_dett_bordero` FOREIGN KEY (`id_bordero`) REFERENCES `bordero` (`id`),
    CONSTRAINT `fk_bordero_dett_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id`),
    CONSTRAINT `fk_bordero_dett_punto_con` FOREIGN KEY (`id_punto_consegna`) REFERENCES `punto_consegna` (`id`),
    CONSTRAINT `fk_bordero_dett_fornitore` FOREIGN KEY (`id_fornitore`) REFERENCES `fornitore` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE contarbn.bordero_riga (
    id int unsigned NOT NULL AUTO_INCREMENT,
    id_bordero int unsigned,
    progressivo int,
    id_cliente int unsigned,
    id_punto_consegna int unsigned,
    id_fornitore int unsigned,
    telefono text,
    note text,
    firma text,
    data_inserimento timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT `fk_bordero_riga_bordero` FOREIGN KEY (`id_bordero`) REFERENCES `bordero` (`id`),
    CONSTRAINT `fk_bordero_riga_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id`),
    CONSTRAINT `fk_bordero_riga_punto_con` FOREIGN KEY (`id_punto_consegna`) REFERENCES `punto_consegna` (`id`),
    CONSTRAINT `fk_bordero_riga_fornitore` FOREIGN KEY (`id_fornitore`) REFERENCES `fornitore` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE VIEW contarbn.v_bordero_riga AS
select
    bordero_riga.id,
    bordero_riga.id_bordero,
    bordero_riga.progressivo,
    bordero_riga.id_cliente,
    null as id_fornitore,
    case
        when cliente.ditta_individuale then
            concat(cliente.cognome, ' ', cliente.nome)
        when cliente.privato then
            concat(cliente.cognome, ' ', cliente.nome)
        else
            concat(cliente.ragione_sociale)
    end as cliente_fornitore,
    bordero_riga.id_punto_consegna,
    concat(coalesce(punto_consegna.indirizzo,''),' ',coalesce(punto_consegna.localita,'')) as punto_consegna,
    bordero_riga.telefono,
    bordero_riga.note,
    bordero_riga.firma
from
    contarbn.bordero_riga
join contarbn.cliente on
    bordero_riga.id_cliente = cliente.id
join contarbn.punto_consegna on
    bordero_riga.id_punto_consegna = punto_consegna.id
where bordero_riga.id_cliente is not null
union all
select
    bordero_riga.id,
    bordero_riga.id_bordero,
    bordero_riga.progressivo,
    null as id_cliente,
    bordero_riga.id_fornitore,
    fornitore.ragione_sociale cliente_fornitore,
    null id_punto_consegna,
    concat(coalesce(fornitore.indirizzo,''),' ',coalesce(fornitore.citta,'')) as punto_consegna,
    bordero_riga.telefono,
    bordero_riga.note,
    bordero_riga.firma
from
    contarbn.bordero_riga
join contarbn.fornitore on
    bordero_riga.id_fornitore = fornitore.id
where bordero_riga.id_fornitore is not null
;
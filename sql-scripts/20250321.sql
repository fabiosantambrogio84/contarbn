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

drop PROCEDURE IF EXISTS contarbn.genera_bordero;
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
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE contarbn.bordero_detail (
    uuid varchar(255),
    id_bordero int unsigned,
    id_cliente int unsigned,
    id_punto_consegna int unsigned,
    id_fornitore int unsigned,
    tipo_documento varchar(255),
    id_documento int unsigned,
    data_consegna date,
    note text,
    data_inserimento timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid),
    CONSTRAINT `fk_bordero_dett_bordero` FOREIGN KEY (`id_bordero`) REFERENCES `bordero` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE TABLE contarbn.bordero_riga (
    uuid varchar(255),
    id_bordero int unsigned,
    progressivo int,
    id_cliente int unsigned,
    id_punto_consegna int unsigned,
    id_fornitore int unsigned,
    telefono text,
    note text,
    firma text,
    data_inserimento timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid),
    CONSTRAINT `fk_bordero_riga_bordero` FOREIGN KEY (`id_bordero`) REFERENCES `bordero` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

CREATE VIEW contarbn.v_bordero_riga AS
select
    bordero_riga.uuid,
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
    cliente.telefono,
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
    bordero_riga.uuid,
    bordero_riga.id_bordero,
    bordero_riga.progressivo,
    null as id_cliente,
    bordero_riga.id_fornitore,
    fornitore.ragione_sociale cliente_fornitore,
    null id_punto_consegna,
    concat(coalesce(fornitore.indirizzo,''),' ',coalesce(fornitore.citta,'')) as punto_consegna,
    concat(fornitore.telefono,coalesce(concat(',', fornitore.telefono_2),''),coalesce(concat(',', fornitore.telefono_3),'')) as telefono,
    bordero_riga.note,
    bordero_riga.firma
from
    contarbn.bordero_riga
        join contarbn.fornitore on
            bordero_riga.id_fornitore = fornitore.id
where bordero_riga.id_fornitore is not null
;

DELIMITER $$

CREATE PROCEDURE contarbn.genera_bordero(IN id_bordero INT, IN id_autista_trasportatore INT, IN data_consegna DATE)
BEGIN
    DECLARE err_msg VARCHAR(255);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN

        GET DIAGNOSTICS CONDITION 1
            err_msg = MESSAGE_TEXT;
        SELECT err_msg AS result;
    END;

    delete from contarbn.bordero_riga where id_bordero=id_bordero;
    delete from contarbn.bordero_detail where id_bordero=id_bordero;

    INSERT INTO contarbn.bordero_detail (uuid, id_bordero, id_cliente, id_punto_consegna, id_fornitore, tipo_documento, id_documento, data_consegna, note, data_inserimento)
    select
        uuid(),
        id_bordero,
        t.id_cliente,
        t.id_punto_consegna,
        t.id_fornitore,
        t.tipo_documento,
        t.id_documento,
        t.data_trasporto,
        t.note,
        current_timestamp()
    from (
             select
                 ddt.id_cliente,
                 null as id_fornitore,
                 ddt.id_punto_consegna,
                 'ddt' as tipo_documento,
                 ddt.id as id_documento,
                 ddt.data_trasporto,
                 ddt.note
             from contarbn.ddt
             where
                 (ddt.consegnato is null or ddt.consegnato = false) and
                     ddt.id_autista = id_autista_trasportatore and
                     ddt.data_trasporto <= data_consegna
             union all
             select
                 fattura_accom.id_cliente,
                 null as id_fornitore,
                 fattura_accom.id_punto_consegna,
                 'fattura_accompagnatoria' as tipo_documento,
                 fattura_accom.id as id_documento,
                 fattura_accom.data_trasporto,
                 fattura_accom.note
             from contarbn.fattura_accom
             where
                 (fattura_accom.consegnato is null or fattura_accom.consegnato = false) and
                     fattura_accom.id_trasportatore = id_autista_trasportatore and
                     fattura_accom.data_trasporto <= data_consegna
             union all
             select
                 ricevuta_privato.id_cliente,
                 null as id_fornitore,
                 ricevuta_privato.id_punto_consegna,
                 'ricevuta_privato' as tipo_documento,
                 ricevuta_privato.id as id_documento,
                 ricevuta_privato.data_trasporto,
                 ricevuta_privato.note
             from contarbn.ricevuta_privato
             where
                 (ricevuta_privato.consegnato is null or ricevuta_privato.consegnato = false) and
                     ricevuta_privato.id_trasportatore = id_autista_trasportatore and
                     ricevuta_privato.data_trasporto <= data_consegna
             union all
             select
                 null as id_cliente,
                 nota_reso.id_fornitore,
                 null as id_punto_consegna,
                 'nota_reso' as tipo_documento,
                 nota_reso.id as id_documento,
                 nota_reso.data_trasporto,
                 nota_reso.note
             from contarbn.nota_reso
             where
                 (nota_reso.consegnato is null or nota_reso.consegnato = false) and
                     nota_reso.id_trasportatore = id_autista_trasportatore and
                     nota_reso.data_trasporto <= data_consegna
         ) t;

    INSERT INTO contarbn.bordero_riga (uuid, id_bordero, id_cliente, id_punto_consegna, id_fornitore, note, data_inserimento)
    select
        uuid(),
        t.id_bordero,
        t.id_cliente,
        t.id_punto_consegna,
        t.id_fornitore,
        t.note,
        current_timestamp()
    from (
             select
                 id_bordero,
                 id_cliente,
                 id_punto_consegna,
                 null as id_fornitore,
                 GROUP_CONCAT(note SEPARATOR ',') as note
             from contarbn.bordero_detail
             where
                id_bordero=id_bordero and
                 id_cliente is not null
             group by
                 id_bordero,
                 id_cliente,
                 id_punto_consegna
             union all
             select
                 id_bordero,
                 null as id_cliente,
                 null as id_punto_consegna,
                 id_fornitore,
                 GROUP_CONCAT(note SEPARATOR ',') as note
             from contarbn.bordero_detail
             where
                id_bordero=id_bordero and
                 id_fornitore is not null
             group by
                 id_bordero,
                 id_fornitore
         ) t;

    SELECT 'OK' AS result;
END $$

DELIMITER ;


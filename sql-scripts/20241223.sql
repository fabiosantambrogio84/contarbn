drop table if exists contarbn.dispositivo;

CREATE TABLE contarbn.dispositivo (
    id int unsigned NOT NULL AUTO_INCREMENT,
    tipo varchar(255),
    nome varchar(255),
    descrizione varchar(255),
    ip varchar(255),
    porta int,
    predefinito bit(1) NOT NULL DEFAULT b'0',
    `attivo` bit(1) NOT NULL DEFAULT b'1',
    `data_inserimento` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `data_aggiornamento` timestamp NULL DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

truncate table contarbn.dispositivo;

INSERT INTO contarbn.dispositivo(tipo, nome, descrizione, ip, porta, predefinito) VALUES('STAMPANTE', 'Zebra GK420d 1', '', '192.168.254.11', 9100, 1);


ALTER TABLE contarbn.etichetta CHANGE html file_content text CHARACTER SET latin1 COLLATE latin1_general_cs NULL;
ALTER TABLE contarbn.etichetta MODIFY COLUMN file_content BLOB NULL;
ALTER TABLE contarbn.etichetta ADD COLUMN id_dispositivo int unsigned after file_content;
alter table contarbn.etichetta add CONSTRAINT `fk_etichetta_dispositivo` FOREIGN KEY (`id_dispositivo`) REFERENCES `dispositivo` (`id`);
CREATE TABLE contarbn.trasportatore (
    `id` int unsigned NOT NULL AUTO_INCREMENT,
    `nome` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
    `cognome` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
    `telefono` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
    `email` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
    `indirizzo` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

update contarbn.ddt
set note=trasportatore
where trasportatore is not null and trasportatore != '';

alter table contarbn.ddt add column id_trasportatore int unsigned after ora_trasporto;
alter table contarbn.ddt add CONSTRAINT `fk_ddt_vend_trasportatore` FOREIGN KEY (`id_trasportatore`) REFERENCES `trasportatore` (`id`);

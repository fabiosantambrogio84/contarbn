-- mysql -u root -p
-- -> chiede password. Inserire root

-- mysql> create user 'contarbn'@'%' identified by 'contarbn';
-- mysql> grant all on contarbn.* to 'contarbn'@'%';

CREATE DATABASE  IF NOT EXISTS `contarbn`;
USE `contarbn`;

SET GLOBAL log_bin_trust_function_creators = 1;

-- import dump

SET GLOBAL log_bin_trust_function_creators = 0;


drop table contarbn.listino_prezzo_20230105;

update contarbn.sequence_data
set sequence_cur_value = 0;

truncate table contarbn.ricevuta_privato_totale;
truncate table contarbn.ricevuta_privato_ordine_cliente;
truncate table contarbn.ricevuta_privato_articolo;
delete from contarbn.ricevuta_privato;

truncate table contarbn.pagamento_aggregato;
truncate table contarbn.pagamento;

truncate table contarbn.nota_reso_totale;
truncate table contarbn.nota_reso_riga;
delete from contarbn.nota_reso;

truncate table contarbn.nota_accredito_totale;
truncate table contarbn.nota_accredito_riga;
delete from contarbn.nota_accredito;

truncate table contarbn.fattura_ddt;

truncate table contarbn.fattura_acquisto_ddt_acquisto;
delete from contarbn.fattura_acquisto;

truncate table contarbn.fattura_accom_totale;
truncate table contarbn.fattura_accom_articolo_ordine_cliente;
truncate table contarbn.fattura_accom_articolo;
delete from contarbn.fattura_accom;

truncate table contarbn.fattura_accom_acquisto_totale;
truncate table contarbn.fattura_accom_acquisto_articolo;
truncate table contarbn.fattura_accom_acquisto_ingrediente;
delete from contarbn.fattura_accom_acquisto;

delete from contarbn.fattura;

truncate table contarbn.ddt_articolo_ordine_cliente;
truncate table contarbn.ddt_articolo;
delete from contarbn.ddt;

truncate table contarbn.ddt_acquisto_articolo;
truncate table contarbn.ddt_acquisto_ingrediente;
delete from contarbn.ddt_acquisto;

truncate table contarbn.ordine_fornitore_articolo;
delete from contarbn.ordine_fornitore;

truncate table contarbn.ordine_cliente_articolo;
delete from contarbn.ordine_cliente;

UPDATE contarbn.fornitore
SET ragione_sociale='URBANI ELIA E MARTA', ragione_sociale_2='', indirizzo='Via 11 Settembre, 17', citta='SAN GIOVANNI ILARIONE', provincia='Verona', cap='37035', nazione='Italy', partita_iva='04998580239', codice_fiscale='', telefono='045 6550993', telefono_2='', telefono_3='', email='info@urbanialimentari.com', email_pec='urbanialimentari@legalmail.it', email_ordini='eliaurbani@gmail.com'
WHERE ragione_sociale='URBANI GIUSEPPE';



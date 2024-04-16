-- GIACENZE
alter table contarbn.movimentazione_manuale_articolo add column compute bit(1) DEFAULT b'1' after context;

-- 02/04/2024
update contarbn.movimentazione_manuale_articolo set context='GIACENZA_ARTICOLO' where operation='CREATE';
-- GIACENZE
alter table contarbn.movimentazione_manuale_articolo add column compute bit(1) DEFAULT b'1' after context;

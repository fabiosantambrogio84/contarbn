create table contarbn.tmp_articoli_to_delete_20241031 as
select id, codice, descrizione
from contarbn.articolo
where codice in (
                 'URRISVEN',
                 'CCSCACAR250',
                 'URAUTO',
                 'CCSCACARC150',
                 'URCOTT'
    )
union
select id, codice, descrizione
-- distinct substring_index(descrizione, ' ', -1) as sub
from contarbn.articolo
where id_fornitore = 23 and
        complete_barcode = 0 and
    (barcode is null or barcode = '') and
    descrizione is not null and descrizione != '' and
descrizione not like '%kg' and
substring_index(descrizione, ' ', -1) in ('0,25gr','0,3gr','1gr','1,2gr','1,5gr','2gr','2,5gr','360x260x150','360x260x250')
;


delete from contarbn.listino_prezzo
where id_articolo in (select id from contarbn.tmp_articoli_to_delete_20241031);

delete from contarbn.giacenza_articolo
where id_articolo in (select id from contarbn.tmp_articoli_to_delete_20241031);

delete from contarbn.movimentazione_manuale_articolo
where id_articolo in (select id from contarbn.tmp_articoli_to_delete_20241031);

update contarbn.produzione_confezione
set id_articolo = null
where id_articolo in (select id from contarbn.tmp_articoli_to_delete_20241031);

delete from contarbn.articolo
where id in (select id from contarbn.tmp_articoli_to_delete_20241031);

drop table contarbn.tmp_articoli_to_delete_20241031;
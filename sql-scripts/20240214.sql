
alter table contarbn.articolo add column scadenza_giorni_allarme int DEFAULT 5 after scadenza_giorni;

ALTER TABLE contarbn.ingrediente CHANGE scadenza_giorni scadenza_giorni_allarme int DEFAULT 5 NULL;
ALTER TABLE contarbn.ingrediente MODIFY COLUMN scadenza_giorni_allarme int DEFAULT 5 NULL;


-- VIEWS
drop view contarbn.v_giacenza_ingrediente_agg;
drop view contarbn.v_giacenza_articolo_agg;
drop view contarbn.v_giacenza_ingrediente;
drop view contarbn.v_giacenza_articolo;
drop view contarbn.v_ingrediente;

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
    articolo.scadenza_giorni_allarme,
    case
        when current_date >= (giacenza_articolo.scadenza - interval coalesce(articolo.scadenza_giorni_allarme,0) DAY) then
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
    ingrediente.scadenza_giorni_allarme,
    (case
         when (curdate() >= (giacenza_ingrediente.scadenza - interval coalesce(ingrediente.scadenza_giorni_allarme, 0) day)) then 1
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

create or replace
algorithm = UNDEFINED view `v_ingrediente` as
select
    `ingrediente`.`id` as `id`,
    `ingrediente`.`codice` as `codice`,
    `ingrediente`.`descrizione` as `descrizione`,
    `ingrediente`.`prezzo` as `prezzo`,
    `ingrediente`.`id_unita_misura` as `id_unita_misura`,
    `ingrediente`.`id_fornitore` as `id_fornitore`,
    (case
         when (`fornitore`.`ragione_sociale` is not null) then `fornitore`.`ragione_sociale`
         else `fornitore`.`ragione_sociale_2`
        end) as `fornitore`,
    `ingrediente`.`id_aliquota_iva` as `id_aliquota_iva`,
    `ingrediente`.`scadenza_giorni_allarme` as `scadenza_giorni_allarme`,
    `ingrediente`.`data_inserimento` as `data_inserimento`,
    `ingrediente`.`composto` as `composto`,
    `ingrediente`.`composizione` as `composizione`,
    `ingrediente`.`attivo` as `attivo`,
    `ingrediente`.`note` as `note`,
    (case
         when (`ingrediente`.`composto` = 0) then `ingrediente`.`descrizione`
         else concat(`ingrediente`.`descrizione`, '(', replace(replace(`ingrediente`.`composizione`, '<p>', ''), '</p>', ''), ')')
        end) as `descrizione_scheda_tecnica`
from
    (`ingrediente`
        join `fornitore` on
            ((`fornitore`.`id` = `ingrediente`.`id_fornitore`)));

-- PROCEDURE
DROP PROCEDURE IF EXISTS contarbn.check_views;

DELIMITER $$
$$
CREATE DEFINER=`contarbn`@`%` PROCEDURE `contarbn`.`check_views`()
BEGIN

	-- Variables to store view name and query
	DECLARE view_name VARCHAR(255);
	DECLARE select_query text;
	DECLARE result_text text;

	DECLARE done BOOLEAN DEFAULT FALSE;

	-- Cursor to loop through views
	DECLARE view_cursor CURSOR FOR
SELECT table_name
FROM information_schema.views
WHERE table_schema = 'contarbn';

DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

	set result_text = '';

	-- Open the cursor
OPEN view_cursor;

-- Loop through views
view_loop: LOOP
	    -- Fetch the next view name
	    FETCH view_cursor INTO view_name;

	    -- Exit the loop if no more views
	    IF done THEN
	        LEAVE view_loop;
END IF;

	    -- Build and execute the select query
	    SET @select_query = CONCAT('SELECT count(*) INTO @id FROM contarbn', '.', view_name);
PREPARE dynamic_query FROM @select_query;
EXECUTE dynamic_query;
DEALLOCATE PREPARE dynamic_query;

if result_text = '' then
	   		SET result_text = CONCAT('Processed views: ', view_name);
else
	   		SET result_text = CONCAT(result_text, '; ', view_name);
end if;

END LOOP;

	-- Close the cursor
CLOSE view_cursor;

SELECT result_text AS final_result;
END$$
DELIMITER ;

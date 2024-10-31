DELIMITER $$
CREATE PROCEDURE `contarbn`.`setArticoliBarcode`()
begin
	DECLARE done INT DEFAULT FALSE;
	DECLARE v_id_articolo INT(10);
	DECLARE v_complete_barcode bit(1);
	declare v_barcode VARCHAR(100);

	DECLARE cur1 CURSOR FOR select id, complete_barcode
                            from contarbn.articolo
                            where id_fornitore = 23 and
                                                                                (barcode is null or barcode = '')
                            order by codice;

DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

OPEN cur1;

read_loop: LOOP
	    FETCH cur1 INTO v_id_articolo,v_complete_barcode;
	    IF done THEN
	      LEAVE read_loop;
END IF;

select
    cast(case when t.complete_barcode = 0 and max(t.barcode) <= 2500030 then 9500031 else max(t.barcode)+1 end as char) as next_barcode
into v_barcode
from (
         select cast(barcode as unsigned) as barcode,complete_barcode
         from articolo
         where id_fornitore = 23 and complete_barcode = v_complete_barcode and barcode is not null and barcode != ''
     ) t;


update articolo set barcode = v_barcode where articolo.id=v_id_articolo;

END LOOP;

CLOSE cur1;
end$$
DELIMITER ;
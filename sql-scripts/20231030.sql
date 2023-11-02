SET GLOBAL log_bin_trust_function_creators = 1;

DROP FUNCTION IF EXISTS contarbn.nextval;

DELIMITER $$
CREATE DEFINER=`root`@`localhost` FUNCTION `contarbn`.`nextval`(`seq_name` varchar(100)) RETURNS bigint
BEGIN
    DECLARE cur_val bigint(20);

SELECT
    sequence_cur_value INTO cur_val
FROM
    contarbn.sequence_data
WHERE
        sequence_name = seq_name;

IF cur_val IS NOT NULL THEN
UPDATE
    contarbn.sequence_data
SET
    sequence_cur_value = IF (
                (sequence_cur_value + sequence_increment) > sequence_max_value,
                IF (
                            sequence_cycle = TRUE,
                            sequence_min_value,
                            NULL
                    ),
                sequence_cur_value + sequence_increment
        )
WHERE
        sequence_name = seq_name;
END IF;

RETURN cur_val;
end$$
DELIMITER ;

SET GLOBAL log_bin_trust_function_creators = 0;
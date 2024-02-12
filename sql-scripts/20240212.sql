
create or replace algorithm = UNDEFINED view `v_scheda_tecnica` as
select
    uuid() as `id`,
    `scheda_tecnica`.`id` as `id_scheda_tecnica`,
    `produzione_confezione`.`id_produzione` as `id_produzione`,
    `produzione_confezione`.`id_articolo` as `id_articolo`,
    `v_ricetta_info`.`id` as `id_ricetta`,
    `scheda_tecnica`.`num_revisione` as `num_revisione`,
    coalesce(`scheda_tecnica`.`anno`, year(curdate())) as `anno`,
    coalesce(`scheda_tecnica`.`data`, curdate()) as `data`,
    coalesce(`scheda_tecnica`.`codice_prodotto`, `articolo`.`codice`) as `codice_prodotto`,
    coalesce(`scheda_tecnica`.`prodotto`, `articolo`.`descrizione`) as `prodotto`,
    coalesce(`scheda_tecnica`.`prodotto_2`, `articolo`.`descrizione_2`) as `prodotto_2`,
    coalesce(`scheda_tecnica`.`peso_netto_confezione`, `confezione`.`tipo`) as `peso_netto_confezione`,
    coalesce(`scheda_tecnica`.`ingredienti`, concat(upper(left(`v_ricetta_info`.`ingredienti`, 1)), lower(substr(`v_ricetta_info`.`ingredienti`, 2)))) as `ingredienti`,
    coalesce(`scheda_tecnica`.`tracce`, `v_ricetta_info`.`allergeni_tracce`) as `tracce`,
    coalesce(`scheda_tecnica`.`durata`, `articolo`.`scadenza_giorni`) as `durata`,
    coalesce(`scheda_tecnica`.`conservazione`, `v_ricetta_info`.`conservazione`) as `conservazione`,
    coalesce(`scheda_tecnica`.`consigli_consumo`, `v_ricetta_info`.`consigli_consumo`) as `consigli_consumo`,
    `scheda_tecnica`.`id_tipologia_confezionamento` as `id_tipologia_confezionamento`,
    `tipologia_confezionamento`.`nome` as `tipologia_confezionamento`,
    `scheda_tecnica`.`id_imballo` as `id_imballo`,
    `imballo`.`nome` as `imballo`,
    `scheda_tecnica`.`imballo_dimensioni` as `imballo_dimensioni`
from
    (((((((`produzione`
        join `produzione_confezione` on
            ((`produzione`.`id` = `produzione_confezione`.`id_produzione`)))
        join `confezione` on
            ((`produzione_confezione`.`id_confezione` = `confezione`.`id`)))
        join `articolo` on
            ((`produzione_confezione`.`id_articolo` = `articolo`.`id`)))
        join `v_ricetta_info` on
            ((`produzione`.`id_ricetta` = `v_ricetta_info`.`id`)))
        left join `scheda_tecnica` on
            (((`produzione_confezione`.`id_produzione` = `scheda_tecnica`.`id_produzione`)
                and (`produzione_confezione`.`id_articolo` = `scheda_tecnica`.`id_articolo`))))
        left join `anagrafica` `tipologia_confezionamento` on
            ((`scheda_tecnica`.`id_tipologia_confezionamento` = `tipologia_confezionamento`.`id`)))
        left join `anagrafica` `imballo` on
        ((`scheda_tecnica`.`id_imballo` = `imballo`.`id`)))
where
    (`produzione`.`tipologia` = 'STANDARD');
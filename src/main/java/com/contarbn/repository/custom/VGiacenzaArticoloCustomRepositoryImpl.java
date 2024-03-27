package com.contarbn.repository.custom;

import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VGiacenzaArticolo;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class VGiacenzaArticoloCustomRepositoryImpl implements VGiacenzaArticoloCustomRepository{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<VGiacenzaArticolo> findByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, String articolo, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto) {
        StringBuilder sb = createQueryAsString("distinct v_giacenza_articolo_agg.*", articolo, attivo, idFornitore, lotto, scadenza, scaduto);

        StringBuilder order = new StringBuilder(" ORDER BY v_giacenza_articolo_agg.articolo ASC, v_giacenza_articolo_agg.pezzi ASC, v_giacenza_articolo_agg.quantita ASC, v_giacenza_articolo_agg.prezzo_listino_base ASC");
        if(draw != null && draw != -1){
            int limit = length != null ? length : 20;
            int offset = start != null ? start : 0;
            if(!sortOrders.isEmpty()){
                int i = 0;
                order = new StringBuilder(" ORDER BY ");
                for(SortOrder sortOrder : sortOrders){
                    order.append(sortOrder.getColumnName()).append(" ").append(sortOrder.getDirection());
                    if(i < (sortOrders.size() - 1)){
                        order.append(",");
                    }
                    i++;
                }
            }

            sb.append(order).append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
        } else {
            sb.append(order);
        }

        Query query = createQuery(sb.toString(), articolo, attivo, idFornitore, lotto, scadenza, scaduto);

        List<Object[]> queryResults = query.getResultList();
        List<VGiacenzaArticolo> result = new ArrayList<>();
        if(queryResults != null && !queryResults.isEmpty()){
            for(Object[] queryResult : queryResults){
                VGiacenzaArticolo vGiacenzaArticolo = new VGiacenzaArticolo();
                vGiacenzaArticolo.setIdArticolo(((Integer)queryResult[0]).longValue());
                if(queryResult[1] != null){
                    vGiacenzaArticolo.setArticolo(((String)queryResult[1]));
                }
                if(queryResult[2] != null){
                    vGiacenzaArticolo.setPrezzoAcquisto((BigDecimal) queryResult[2]);
                }
                if(queryResult[3] != null){
                    vGiacenzaArticolo.setPrezzoListinoBase((BigDecimal)queryResult[3]);
                }
                if(queryResult[4] != null){
                    vGiacenzaArticolo.setAttivo((Boolean)queryResult[4]);
                }
                if(queryResult[5] != null){
                    vGiacenzaArticolo.setIdFornitore(((Integer)queryResult[5]).longValue());
                }
                if(queryResult[6] != null){
                    vGiacenzaArticolo.setFornitore(((String)queryResult[6]));
                }
                if(queryResult[7] != null){
                    vGiacenzaArticolo.setUnitaMisura(((String)queryResult[7]));
                }
                if(queryResult[8] != null){
                    vGiacenzaArticolo.setQuantita(((BigDecimal)queryResult[8]).floatValue());
                }
                if(queryResult[9] != null){
                    vGiacenzaArticolo.setPezzi(((BigInteger)queryResult[9]).intValue());
                }
                if(queryResult[10] != null){
                    vGiacenzaArticolo.setQuantitaResult(((BigDecimal)queryResult[10]).floatValue());
                }
                if(queryResult[11] != null){
                    vGiacenzaArticolo.setTotale(((BigDecimal)queryResult[11]).floatValue());
                }
                if(queryResult[12] != null){
                    vGiacenzaArticolo.setScaduto(((Integer)queryResult[12]));
                }
                result.add(vGiacenzaArticolo);
            }
        }

        return result;
    }

    @Override
    public Integer countByFilters(String articolo, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto) {
        int count = 0;

        StringBuilder sb = createQueryAsString("count(distinct v_giacenza_articolo_agg.id_articolo)", articolo, attivo, idFornitore, lotto, scadenza, scaduto);

        Query query = createQuery(sb.toString(), articolo, attivo, idFornitore, lotto, scadenza, scaduto);

        Object result = query.getSingleResult();
        if(result != null){
            count = ((BigInteger)result).intValue();
        }
        return count;
    }

    private StringBuilder createQueryAsString(String select, String articolo, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto){
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(select);
        sb.append(" FROM contarbn.v_giacenza_articolo_agg");

        if(StringUtils.isNotEmpty(lotto) || scadenza != null){
            sb.append(" JOIN giacenza_articolo ON v_giacenza_articolo_agg.id_articolo = giacenza_articolo.id_articolo");
        }

        sb.append(" WHERE 1=1 ");
        sb.append(" AND (coalesce(v_giacenza_articolo_agg.quantita, 0) != 0 OR coalesce(v_giacenza_articolo_agg.pezzi, 0) != 0) ");
        if(StringUtils.isNotEmpty(articolo)) {
            sb.append(" AND lower(v_giacenza_articolo_agg.articolo) LIKE concat('%',:articolo,'%') ");
        }
        if(attivo != null) {
            sb.append(" AND v_giacenza_articolo_agg.attivo = :attivo ");
        }
        if(idFornitore != null) {
            sb.append(" AND v_giacenza_articolo_agg.id_fornitore = :idFornitore ");
        }
        if(StringUtils.isNotEmpty(lotto)) {
            sb.append(" AND lower(giacenza_articolo.lotto) LIKE concat('%',:lotto,'%') ");
        }
        if(scadenza != null){
            sb.append(" AND giacenza_articolo.scadenza = :scadenza ");
        }
        if(scaduto != null){
            sb.append(" AND v_giacenza_articolo_agg.scaduto = :scaduto ");
        }
        return sb;
    }

    private Query createQuery(String queryAsString, String articolo, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto) {
        Query query = entityManager.createNativeQuery(queryAsString);
        if(StringUtils.isNotEmpty(articolo)) {
            query.setParameter("articolo", articolo);
        }
        if(attivo != null) {
            query.setParameter("attivo", attivo);
        }
        if(idFornitore != null) {
            query.setParameter("idFornitore", idFornitore);
        }
        if(StringUtils.isNotEmpty(lotto)){
            query.setParameter("lotto", lotto);
        }
        if(scadenza != null) {
            query.setParameter("scadenza", scadenza);
        }
        if(scaduto != null) {
            query.setParameter("scaduto", scaduto);
        }
        return query;
    }
}
package com.contarbn.repository.custom;

import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VGiacenzaIngrediente;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class VGiacenzaIngredienteCustomRepositoryImpl implements VGiacenzaIngredienteCustomRepository{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<VGiacenzaIngrediente> findByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, String ingrediente, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto) {
        StringBuilder sb = createQueryAsString("distinct v_giacenza_ingrediente_agg.*", ingrediente, attivo, idFornitore, lotto, scadenza, scaduto);

        StringBuilder order = new StringBuilder(" ORDER BY v_giacenza_ingrediente_agg.ingrediente ASC, v_giacenza_ingrediente_agg.fornitore ASC, v_giacenza_ingrediente_agg.quantita_tot ASC");
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

        Query query = createQuery(sb.toString(), ingrediente, attivo, idFornitore, lotto, scadenza, scaduto);

        List<Object[]> queryResults = query.getResultList();
        List<VGiacenzaIngrediente> result = new ArrayList<>();
        if(queryResults != null && !queryResults.isEmpty()){
            for(Object[] queryResult : queryResults){
                VGiacenzaIngrediente vGiacenzaIngrediente = new VGiacenzaIngrediente();
                vGiacenzaIngrediente.setIdIngrediente(((Integer)queryResult[0]).longValue());
                if(queryResult[1] != null){
                    vGiacenzaIngrediente.setIngrediente(((String)queryResult[1]));
                }
                if(queryResult[2] != null){
                    vGiacenzaIngrediente.setQuantita(((BigDecimal)queryResult[2]).floatValue());
                }
                if(queryResult[3] != null){
                    vGiacenzaIngrediente.setAttivo((Boolean)queryResult[3]);
                }
                if(queryResult[4] != null){
                    vGiacenzaIngrediente.setUdm(((String)queryResult[4]));
                }
                if(queryResult[5] != null){
                    vGiacenzaIngrediente.setIdFornitore(((Integer)queryResult[5]).longValue());
                }
                if(queryResult[6] != null){
                    vGiacenzaIngrediente.setFornitore(((String)queryResult[6]));
                }
                if(queryResult[7] != null){
                    vGiacenzaIngrediente.setCodiceIngrediente(((String)queryResult[7]));
                }
                if(queryResult[8] != null){
                    vGiacenzaIngrediente.setDescrizioneIngrediente(((String)queryResult[8]));
                }
                if(queryResult[9] != null){
                    vGiacenzaIngrediente.setScaduto(((Integer)queryResult[9]));
                }
                result.add(vGiacenzaIngrediente);
            }
        }

        return result;
    }

    @Override
    public Integer countByFilters(String ingrediente, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto) {
        int count = 0;

        StringBuilder sb = createQueryAsString("count(distinct v_giacenza_ingrediente_agg.id_ingrediente)", ingrediente, attivo, idFornitore, lotto, scadenza, scaduto);

        Query query = createQuery(sb.toString(), ingrediente, attivo, idFornitore, lotto, scadenza, scaduto);

        Object result = query.getSingleResult();
        if(result != null){
            count = ((BigInteger)result).intValue();
        }
        return count;
    }

    private StringBuilder createQueryAsString(String select, String ingrediente, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto){
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(select);
        sb.append(" FROM contarbn.v_giacenza_ingrediente_agg");

        if(StringUtils.isNotEmpty(lotto) || scadenza != null){
            sb.append(" JOIN giacenza_ingrediente ON v_giacenza_ingrediente_agg.id_ingrediente = giacenza_ingrediente.id_ingrediente");
        }

        sb.append(" WHERE 1=1 ");
        sb.append(" AND coalesce(v_giacenza_ingrediente_agg.quantita_tot, 0) != 0 ");
        if(StringUtils.isNotEmpty(ingrediente)) {
            sb.append(" AND lower(v_giacenza_ingrediente_agg.ingrediente) LIKE concat('%',:ingrediente,'%') ");
        }
        if(attivo != null) {
            sb.append(" AND v_giacenza_ingrediente_agg.attivo = :attivo ");
        }
        if(idFornitore != null) {
            sb.append(" AND v_giacenza_ingrediente_agg.id_fornitore = :idFornitore ");
        }
        if(StringUtils.isNotEmpty(lotto)) {
            sb.append(" AND lower(giacenza_ingrediente.lotto) LIKE concat('%',:lotto,'%') ");
        }
        if(scadenza != null){
            sb.append(" AND giacenza_ingrediente.scadenza = :scadenza ");
        }
        if(scaduto != null){
            sb.append(" AND v_giacenza_ingrediente_agg.scaduto = :scaduto ");
        }
        return sb;
    }

    private Query createQuery(String queryAsString, String ingrediente, Boolean attivo, Integer idFornitore, String lotto, Date scadenza, Boolean scaduto) {
        Query query = entityManager.createNativeQuery(queryAsString);
        if(StringUtils.isNotEmpty(ingrediente)) {
            query.setParameter("ingrediente", ingrediente);
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
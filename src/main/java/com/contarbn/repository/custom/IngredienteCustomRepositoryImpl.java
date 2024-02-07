package com.contarbn.repository.custom;

import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VIngrediente;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class IngredienteCustomRepositoryImpl implements IngredienteCustomRepository{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<VIngrediente> findByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, String codice, String descrizione, Integer idFornitore, Boolean composto, Boolean attivo) {
        StringBuilder sb = createQueryAsString("*", codice, descrizione, idFornitore, composto, attivo);

        if(draw != null && draw != -1){
            int limit = length != null ? length : 20;
            int offset = start != null ? start : 0;
            String order = " ORDER BY attivo DESC, codice";
            if(!sortOrders.isEmpty()){
                int i = 0;
                order = " ORDER BY ";
                for(SortOrder sortOrder : sortOrders){
                    order += sortOrder.getColumnName() + " " + sortOrder.getDirection();
                    if(i < (sortOrders.size() - 1)){
                        order += ",";
                    }
                    i++;
                }
            }

            sb.append(order).append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
        } else {
            sb.append(" ORDER BY attivo DESC, codice");
        }

        Query query = createQuery(sb.toString(), codice, descrizione, idFornitore, composto, attivo);

        List<Object[]> queryResults = query.getResultList();
        List<VIngrediente> result = new ArrayList<>();
        if(queryResults != null && !queryResults.isEmpty()){
            for(Object[] queryResult : queryResults){
                VIngrediente vIngrediente = new VIngrediente();
                vIngrediente.setId(((Integer)queryResult[0]).longValue());
                if(queryResult[1] != null){
                    vIngrediente.setCodice((String)queryResult[1]);
                }
                if(queryResult[2] != null){
                    vIngrediente.setDescrizione((String)queryResult[2]);
                }
                if(queryResult[3] != null){
                    vIngrediente.setPrezzo((BigDecimal) queryResult[3]);
                }
                if(queryResult[4] != null){
                    vIngrediente.setIdUnitaMisura(((Integer)queryResult[4]).longValue());
                }
                if(queryResult[5] != null){
                    vIngrediente.setIdFornitore(((Integer)queryResult[5]).longValue());
                }
                if(queryResult[6] != null){
                    vIngrediente.setFornitore((String)queryResult[6]);
                }
                if(queryResult[7] != null){
                    vIngrediente.setIdAliquotaIva(((Integer)queryResult[7]).longValue());
                }
                if(queryResult[8] != null){
                    vIngrediente.setScadenzaGiorni((Integer)queryResult[8]);
                }
                if(queryResult[9] != null){
                    vIngrediente.setDataInserimento((Timestamp)queryResult[9]);
                }
                if(queryResult[10] != null){
                    vIngrediente.setComposto((Boolean)queryResult[10]);
                }
                if(queryResult[11] != null){
                    vIngrediente.setComposizione((String)queryResult[11]);
                }
                if(queryResult[12] != null){
                    vIngrediente.setAttivo((Boolean)queryResult[12]);
                }
                if(queryResult[13] != null){
                    vIngrediente.setNote((String)queryResult[13]);
                }
                result.add(vIngrediente);
            }
        }

        return result;
    }

    @Override
    public Integer countByFilters(String codice, String descrizione, Integer idFornitore, Boolean composto, Boolean attivo) {
        int count = 0;

        StringBuilder sb = createQueryAsString("count(1)", codice, descrizione, idFornitore, composto, attivo);

        Query query = createQuery(sb.toString(), codice, descrizione, idFornitore, composto, attivo);

        Object result = query.getSingleResult();
        if(result != null){
            count = ((BigInteger)result).intValue();
        }
        return count;
    }

    private StringBuilder createQueryAsString(String select, String codice, String descrizione, Integer idFornitore, Boolean composto, Boolean attivo){
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(select);
        sb.append(" FROM contarbn.v_ingrediente WHERE 1=1 ");

        if(codice != null) {
            sb.append(" AND lower(codice) LIKE concat('%',:codice,'%') ");
        }
        if(descrizione != null) {
            sb.append(" AND lower(descrizione) LIKE concat('%',:descrizione,'%') ");
        }
        if(idFornitore != null) {
            sb.append(" AND id_fornitore = :idFornitore ");
        }
        if(composto != null) {
            sb.append(" AND composto = :composto ");
        }
        if(attivo != null) {
            sb.append(" AND attivo = :attivo ");
        }
        return sb;
    }

    private Query createQuery(String queryAsString, String codice, String descrizione, Integer idFornitore, Boolean composto, Boolean attivo) {
        Query query = entityManager.createNativeQuery(queryAsString);

        if(codice != null) {
            query.setParameter("codice", codice);
        }
        if(descrizione != null) {
            query.setParameter("descrizione", descrizione);
        }
        if(idFornitore != null) {
            query.setParameter("idFornitore", idFornitore);
        }
        if(composto != null) {
            query.setParameter("composto", composto);
        }
        if(attivo != null) {
            query.setParameter("attivo", attivo);
        }
        return query;
    }
}

package com.contarbn.repository.custom;

import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VSchedaTecnicaLight;
import org.apache.commons.lang.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class VSchedaTecnicaLightCustomRepositoryImpl implements VSchedaTecnicaLightCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<VSchedaTecnicaLight> findByFilters(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, String prodotto) {
        StringBuilder sb = createQueryAsString("*", prodotto);

        if(draw != null && draw != -1){
            int limit = length != null ? length : 20;
            int offset = start != null ? start : 0;
            String order = " ORDER BY prodotto_descr";
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
            sb.append(" ORDER BY prodotto_descr");
        }

        Query query = createQuery(sb.toString(), prodotto);

        List<Object[]> queryResults = query.getResultList();
        List<VSchedaTecnicaLight> result = new ArrayList<>();
        if(queryResults != null && !queryResults.isEmpty()){
            for(Object[] queryResult : queryResults){
                VSchedaTecnicaLight vSchedaTecnicaLight = new VSchedaTecnicaLight();
                vSchedaTecnicaLight.setId(((Integer)queryResult[0]).longValue());
                if(queryResult[1] != null){
                    vSchedaTecnicaLight.setIdProduzione(((Integer)queryResult[1]).longValue());
                }
                if(queryResult[2] != null){
                    vSchedaTecnicaLight.setIdArticolo(((Integer)queryResult[2]).longValue());
                }
                if(queryResult[3] != null){
                    vSchedaTecnicaLight.setCodiceProdotto((String) queryResult[3]);
                }
                if(queryResult[4] != null){
                    vSchedaTecnicaLight.setProdotto((String) queryResult[4]);
                }
                if(queryResult[5] != null){
                    vSchedaTecnicaLight.setProdottoDescr((String)queryResult[5]);
                }
                if(queryResult[6] != null){
                    vSchedaTecnicaLight.setNumRevisione(((Integer)queryResult[6]));
                }
                if(queryResult[7] != null){
                    vSchedaTecnicaLight.setData((Date)queryResult[7]);
                }
                result.add(vSchedaTecnicaLight);
            }
        }

        return result;
    }

    @Override
    public Integer countByFilters(String prodotto) {
        int count = 0;

        StringBuilder sb = createQueryAsString("count(1)", prodotto);

        Query query = createQuery(sb.toString(), prodotto);

        Object result = query.getSingleResult();
        if(result != null){
            count = ((BigInteger)result).intValue();
        }
        return count;
    }

    private StringBuilder createQueryAsString(String select, String prodotto){
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(select);
        sb.append(" FROM contarbn.v_scheda_tecnica_light WHERE 1=1 ");

        if(StringUtils.isNotEmpty(prodotto)) {
            sb.append(" AND lower(prodotto_descr) LIKE concat('%',:prodotto,'%') ");
        }
        return sb;
    }

    private Query createQuery(String queryAsString, String prodotto) {
        Query query = entityManager.createNativeQuery(queryAsString);

        if(StringUtils.isNotEmpty(prodotto)) {
            query.setParameter("prodotto", prodotto);
        }
        return query;
    }
}

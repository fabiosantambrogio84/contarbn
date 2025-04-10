package com.contarbn.repository.custom;

import com.contarbn.model.beans.SortOrder;
import com.contarbn.model.views.VBorderoRiga;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class VBorderoRigaRepositoryImpl implements VBorderoRigaRepository{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<VBorderoRiga> findByIdBordero(Integer draw, Integer start, Integer length, List<SortOrder> sortOrders, Long idBordero) {
        StringBuilder sb = createQueryAsString("*", idBordero);

        if(draw != null && draw != -1){
            int limit = length != null ? length : 20;
            int offset = start != null ? start : 0;
            StringBuilder order = new StringBuilder(" ORDER BY progressivo ASC, cliente_fornitore ASC, punto_consegna ASC");
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
            sb.append(" ORDER BY progressivo ASC, cliente_fornitore ASC, punto_consegna ASC");
        }

        Query query = createQuery(sb.toString(), idBordero);

        List<Object[]> queryResults = query.getResultList();
        List<VBorderoRiga> result = new ArrayList<>();
        if(queryResults != null && !queryResults.isEmpty()){
            for(Object[] queryResult : queryResults){
                result.add(createFromQueryResult(queryResult));
            }
        }
        return result;
    }

    @Override
    public Integer countByIdBordero(Long idBordero) {
        int count = 0;

        StringBuilder sb = createQueryAsString("count(1)", idBordero);

        Query query = createQuery(sb.toString(), idBordero);

        Object result = query.getSingleResult();
        if(result != null){
            count = ((BigInteger)result).intValue();
        }
        return count;
    }

    @Override
    public VBorderoRiga findByIdBorderoRiga(String uuid) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT *");
        sb.append(" FROM contarbn.v_bordero_riga WHERE 1=1 ");

        if(uuid != null) {
            sb.append(" AND uuid = :uuid ");
        }

        Query query = entityManager.createNativeQuery(sb.toString());

        if(uuid != null) {
            query.setParameter("uuid", uuid);
        }

        Object[] queryResult = (Object[])query.getSingleResult();

        return createFromQueryResult(queryResult);
    }

    private StringBuilder createQueryAsString(String select, Long idBordero){
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(select);
        sb.append(" FROM contarbn.v_bordero_riga WHERE 1=1 ");

        if(idBordero != null) {
            sb.append(" AND id_bordero = :idBordero ");
        }
        return sb;
    }

    private Query createQuery(String queryAsString, Long idBordero) {
        Query query = entityManager.createNativeQuery(queryAsString);

        if(idBordero != null) {
            query.setParameter("idBordero", idBordero);
        }
        return query;
    }

    private VBorderoRiga createFromQueryResult(Object[] queryResult){
        VBorderoRiga vBorderoRiga = new VBorderoRiga();
        vBorderoRiga.setUuid((String)queryResult[0]);
        if(queryResult[1] != null){
            vBorderoRiga.setIdBordero((Integer)queryResult[1]);
        }
        if(queryResult[2] != null){
            vBorderoRiga.setProgressivo((Integer)queryResult[2]);
        }
        if(queryResult[3] != null){
            vBorderoRiga.setIdCliente((Integer)queryResult[3]);
        }
        if(queryResult[4] != null){
            vBorderoRiga.setIdFornitore((Integer)queryResult[4]);
        }
        if(queryResult[5] != null){
            vBorderoRiga.setClienteFornitore((String)queryResult[5]);
        }
        if(queryResult[6] != null){
            vBorderoRiga.setIdPuntoConsegna((Integer)queryResult[6]);
        }
        if(queryResult[7] != null){
            vBorderoRiga.setPuntoConsegna(((String)queryResult[7]));
        }
        if(queryResult[8] != null){
            vBorderoRiga.setTelefono((String)queryResult[8]);
        }
        if(queryResult[9] != null){
            vBorderoRiga.setNote((String)queryResult[9]);
        }
        if(queryResult[10] != null){
            vBorderoRiga.setFirma((String)queryResult[10]);
        }
        return vBorderoRiga;
    }
}

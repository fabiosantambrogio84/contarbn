package com.contarbn.repository.custom;

import com.contarbn.model.MovimentazioneManualeArticolo;
import com.contarbn.util.enumeration.Operation;
import com.contarbn.util.enumeration.Resource;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.sql.Date;
import java.util.Optional;

@Slf4j
public class MovimentazioneManualeArticoloCustomRepositoryImpl implements MovimentazioneManualeArticoloCustomRepository{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<MovimentazioneManualeArticolo> findLastByOperationAndContext(Operation operation, Resource resource, Long idArticolo, String lotto, Date scadenza) {
        StringBuilder sb = createQueryAsString("*", operation, resource, idArticolo, lotto, scadenza);
        sb.append(" ORDER BY COALESCE(data_aggiornamento, data_inserimento) DESC LIMIT 1");

        Query query = createQuery(sb.toString(), operation, resource, idArticolo, lotto, scadenza);

        Optional<MovimentazioneManualeArticolo> result = Optional.empty();

        try{
            MovimentazioneManualeArticolo movimentazioneManualeArticolo = (MovimentazioneManualeArticolo)query.getSingleResult();
            if(movimentazioneManualeArticolo != null){
                result = Optional.of(movimentazioneManualeArticolo);
            }
        } catch(Exception e){
            log.error("Error executing query", e);
        }

        return result;
    }

    private StringBuilder createQueryAsString(String select, Operation operation, Resource resource, Long idArticolo, String lotto, Date scadenza){
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(select);
        sb.append(" FROM contarbn.movimentazione_manuale_articolo WHERE 1=1 ");

        if(operation != null) {
            sb.append(" AND operation = :operation ");
        }
        if(resource != null) {
            sb.append(" AND context = :resource ");
        }
        if(idArticolo != null) {
            sb.append(" AND id_articolo = :idArticolo ");
        }
        if(lotto != null) {
            sb.append(" AND lotto = :lotto ");
        }
        if(scadenza != null) {
            sb.append(" AND scadenza = :scadenza ");
        }
        return sb;
    }

    private Query createQuery(String queryAsString, Operation operation, Resource resource, Long idArticolo, String lotto, Date scadenza) {
        Query query = entityManager.createNativeQuery(queryAsString);

        if(operation != null) {
            query.setParameter("operation", operation.name());
        }
        if(resource != null) {
            query.setParameter("resource", resource.name());
        }
        if(idArticolo != null) {
            query.setParameter("idArticolo", idArticolo);
        }
        if(lotto != null) {
            query.setParameter("lotto", lotto);
        }
        if(scadenza != null) {
            query.setParameter("scadenza", scadenza);
        }
        return query;
    }
}

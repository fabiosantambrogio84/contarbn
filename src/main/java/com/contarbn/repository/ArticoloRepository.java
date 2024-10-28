package com.contarbn.repository;

import com.contarbn.model.Articolo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ArticoloRepository extends CrudRepository<Articolo, Long> {

    @Override
    Set<Articolo> findAll();

    Set<Articolo> findAllByOrderByCodiceAsc();

    Optional<Articolo> findByCodice(String codice);

    Set<Articolo> findByAttivoOrderByCodiceAsc(Boolean attivo);

    Set<Articolo> findByAttivoAndFornitoreId(Boolean attivo, Long idFornitore);

    Set<Articolo> findByAttivoAndBarcodeEqualsAndCompleteBarcodeIsTrue(Boolean attivo, String barcode);

    Set<Articolo> findByAttivoAndBarcodeStartsWithAndCompleteBarcodeIsFalse(Boolean attivo, String barcode);

    @Query(value = "SELECT * FROM articolo WHERE codice LIKE ?1 AND attivo in ?2 ORDER BY codice", nativeQuery = true)
    List<Articolo> findByCodiceLike(String codice, List<Integer> activeValues);

    @Query(value = "select cast(case when t.complete_barcode = 0 and max(t.barcode) <= 2500030 then 9500031 else max(t.barcode)+1 end as char) as next_barcode " +
            "from ( select cast(barcode as unsigned) as barcode,complete_barcode " +
            "from articolo " +
            "where id_fornitore = ?1 and complete_barcode = ?2 and barcode is not null and barcode != '') t", nativeQuery = true)
    String findNextBarcode(Long idFornitore, Integer completeBarcode);
}
package com.contarbn.repository;

import com.contarbn.model.Articolo;
import org.springframework.data.repository.CrudRepository;

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

}

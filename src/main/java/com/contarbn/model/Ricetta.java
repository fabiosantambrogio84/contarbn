package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"ricettaIngredienti", "produzioni", "ricettaAllergeni"})
@Entity
@Table(name = "ricetta")
public class Ricetta {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codice")
    private String codice;

    @Column(name = "nome")
    private String nome;

    @Column(name = "nome_2")
    private String nome2;

    @ManyToOne
    @JoinColumn(name="id_categoria")
    private CategoriaRicetta categoria;

    @Column(name = "tempo_preparazione")
    private Integer tempoPreparazione;

    @Column(name = "peso_totale")
    private Float pesoTotale;

    @Column(name = "scadenza_giorni")
    private Integer scadenzaGiorni;

    @Column(name = "costo_ingredienti")
    private BigDecimal costoIngredienti;

    @Column(name = "costo_preparazione")
    private BigDecimal costoPreparazione;

    @Column(name = "costo_totale")
    private BigDecimal costoTotale;

    @Column(name = "preparazione")
    private String preparazione;

    @Column(name = "allergeni")
    private String allergeni;

    @Column(name = "valori_nutrizionali")
    private String valoriNutrizionali;

    @Column(name = "conservazione")
    private String conservazione;

    @Column(name = "note")
    private String note;

    @OneToMany(mappedBy = "ricetta")
    @JsonIgnoreProperties("ricetta")
    private Set<RicettaIngrediente> ricettaIngredienti = new HashSet<>();

    @OneToMany(mappedBy = "ricetta")
    @JsonIgnoreProperties("ricetta")
    private Set<RicettaAllergene> ricettaAllergeni = new HashSet<>();

    @OneToMany(mappedBy = "ricetta")
    @JsonIgnore
    private List<Produzione> produzioni;

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("{");
        result.append("id: ").append(id);
        result.append(", codice: ").append(codice);
        result.append(", nome: ").append(nome);
        result.append(", nome2: ").append(nome2);
        result.append(", categoria: ").append(categoria);
        result.append(", tempoPreparazione: ").append(tempoPreparazione);
        result.append(", pesoTotale: ").append(pesoTotale);
        result.append(", scadenzaGiorni: ").append(scadenzaGiorni);
        result.append(", costoIngredienti: ").append(costoIngredienti);
        result.append(", costoPreparazione: ").append(costoPreparazione);
        result.append(", costoTotale: ").append(costoTotale);
        result.append(", preparazione: ").append(preparazione);
        result.append(", allergeni: ").append(allergeni);
        result.append(", valoriNutrizionali: ").append(valoriNutrizionali);
        result.append(", conservazione: ").append(conservazione);
        result.append(", note: ").append(note);
        result.append(", ingredienti: [");
        for(RicettaIngrediente ricettaIngrediente: ricettaIngredienti){
            result.append("{");
            result.append(ricettaIngrediente.toString());
            result.append("}");
        }
        result.append("]");
        result.append(", allergeni: [");
        for(RicettaAllergene ricettaAllergene: ricettaAllergeni){
            result.append("{");
            result.append(ricettaAllergene.toString());
            result.append("}");
        }
        result.append("}");

        return result.toString();
    }
}

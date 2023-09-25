package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@EqualsAndHashCode(exclude = {"ricette", "produzioni"})
@Entity
@Table(name = "categoria_ricetta")
public class CategoriaRicetta {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String nome;

    @Column(name = "ordine")
    private Integer ordine;

    @OneToMany(mappedBy = "categoria")
    @JsonIgnore
    private List<Ricetta> ricette;

    @OneToMany(mappedBy = "categoria")
    @JsonIgnore
    private List<Produzione> produzioni;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getOrdine() {
        return ordine;
    }

    public void setOrdine(Integer ordine) {
        this.ordine = ordine;
    }

    public List<Ricetta> getRicette() {
        return ricette;
    }

    public void setRicette(List<Ricetta> ricette) {
        this.ricette = ricette;
    }

    public List<Produzione> getProduzioni() {
        return produzioni;
    }

    public void setProduzioni(List<Produzione> produzioni) {
        this.produzioni = produzioni;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("{");
        result.append("id: " + id);
        result.append(", nome: " + nome);
        result.append(", ordine: " + ordine);
        result.append("}");

        return result.toString();

    }
}

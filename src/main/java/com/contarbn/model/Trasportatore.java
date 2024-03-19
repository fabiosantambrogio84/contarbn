package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@EqualsAndHashCode
@Entity
@Table(name = "trasportatore")
@Data
public class Trasportatore {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String nome;

    @Column(name = "cognome")
    private String cognome;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "email")
    private String email;

    @Column(name = "indirizzo")
    private String indirizzo;

    @Transient
    private boolean predefinito;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", nome: " + nome +
                ", cognome: " + cognome +
                ", telefono: " + telefono +
                ", email: " + email +
                ", indirizzo: " + indirizzo +
                ", predefinito: " + predefinito +
                "}";
    }
}

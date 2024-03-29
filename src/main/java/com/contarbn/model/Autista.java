package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"telefonate"})
@Entity
@Table(name = "autista")
public class Autista {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome")
    private String nome;

    @Column(name = "cognome")
    private String cognome;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "attivo")
    private Boolean attivo;

    @OneToMany(mappedBy = "autista")
    @JsonIgnore
    private List<Telefonata> telefonate;

    @Transient
    private boolean predefinito;

    @Override
    public String toString() {

        return "{" +
                "id: " + id +
                ", nome: " + nome +
                ", cognome: " + cognome +
                ", telefono: " + telefono +
                "}";

    }
}

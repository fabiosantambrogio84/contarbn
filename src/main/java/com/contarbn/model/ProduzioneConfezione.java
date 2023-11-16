package com.contarbn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "produzione_confezione")
public class ProduzioneConfezione implements Serializable {

    private static final long serialVersionUID = -7611851738261520234L;

    @EmbeddedId
    ProduzioneConfezioneKey id;

    @ManyToOne
    @MapsId("id_produzione")
    @JoinColumn(name = "id_produzione")
    @JsonIgnoreProperties("produzioneConfezioni")
    private Produzione produzione;

    @ManyToOne
    @MapsId("id_confezione")
    @JoinColumn(name = "id_confezione")
    @JsonIgnoreProperties("produzioneConfezioni")
    private Confezione confezione;

    @Column(name = "num_confezioni")
    private Integer numConfezioni;

    @Column(name = "lotto")
    private String lotto;

    @Column(name = "lotto_2")
    private String lotto2;

    @Column(name = "lotto_produzione")
    private String lottoProduzione;

    @Column(name = "lotto_film_chiusura")
    private String lottoFilmChiusura;

    @Column(name = "num_confezioni_prodotte")
    private Integer numConfezioniProdotte;

    @ManyToOne
    @JoinColumn(name="id_articolo")
    private Articolo articolo;

    @ManyToOne
    @JoinColumn(name="id_ingrediente")
    private Ingrediente ingrediente;

    @Override
    public String toString() {

        return "{" +
                "produzioneId: " + id.produzioneId +
                ", confezioneId: " + id.confezioneId +
                ", numConfezioni: " + numConfezioni +
                ", lotto: " + lotto +
                ", lotto2: " + lotto2 +
                ", lottoProduzione: " + lottoProduzione +
                ", lottoFilmChiusura: " + lottoFilmChiusura +
                ", numConfezioniProdotte: " + numConfezioniProdotte +
                ", articolo: " + articolo +
                ", ingrediente: " + ingrediente +
                "}";
    }
}

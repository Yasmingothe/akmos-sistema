package br.unitins.akmos.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Um clique em um botão de WhatsApp da landing page.
 * Não guarda nenhum dado pessoal do visitante (nem IP, nem nome, nem telefone).
 */
@Entity
@Table(name = "clique")
public class Clique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoClique tipo;

    /** Nome do kit ou produto (ex.: "Kit AK Body") ou texto do botão. */
    @Column(nullable = false, length = 120)
    private String item;

    /** Parte da página onde estava o botão (ex.: "kits", "produtos", "topo"). */
    @Column(length = 40)
    private String secao;

    /** "celular" ou "computador". */
    @Column(length = 15)
    private String dispositivo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    /** Guardado separado para facilitar o relatório por dia. */
    @Column(nullable = false)
    private LocalDate data;

    @PrePersist
    void preencherData() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now();
        }
        data = dataHora.toLocalDate();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoClique getTipo() {
        return tipo;
    }

    public void setTipo(TipoClique tipo) {
        this.tipo = tipo;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getSecao() {
        return secao;
    }

    public void setSecao(String secao) {
        this.secao = secao;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }
}

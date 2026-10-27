package br.unitins.akmos.model;

import java.math.BigDecimal;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Produto Akmos vendido pelo revendedor. */
@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    /** Ex.: "Controle de peso", "Beleza", "Treino". */
    @Column(length = 40)
    private String categoria;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer estoque = 0;

    /** Aparece na seção "Produtos em destaque" do site. */
    @Column(nullable = false)
    private Boolean destaque = false;

    /** Produto inativo não aparece no site (mas continua cadastrado). */
    @Column(nullable = false)
    private Boolean ativo = true;

    /** Foto padrão que já vem com o site (ex.: /img/carbo-fire.png). */
    @Column(name = "imagem_arquivo", length = 150)
    private String imagemArquivo;

    /** Foto enviada pelo painel. Só é carregada do banco quando alguém pede a imagem. */
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "imagem", length = 5_000_000)
    private byte[] imagem;

    /** Tipo da foto enviada (image/jpeg, image/png...). Nulo = não tem foto enviada. */
    @Column(name = "imagem_tipo", length = 30)
    private String imagemTipo;

    /** Muda a cada foto nova, para o navegador não mostrar a foto antiga guardada. */
    @Column(name = "imagem_versao")
    private Long imagemVersao;

    public boolean isDisponivel() {
        return Boolean.TRUE.equals(ativo) && estoque != null && estoque > 0;
    }

    /** Endereço da foto: a enviada pelo painel ou, se não tiver, a padrão do site. */
    public String getImagemUrl() {
        if (imagemTipo != null) {
            return "/api/produtos/" + id + "/imagem?v=" + imagemVersao;
        }
        return imagemArquivo;
    }

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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public Boolean getDestaque() {
        return destaque;
    }

    public void setDestaque(Boolean destaque) {
        this.destaque = destaque;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public String getImagemArquivo() {
        return imagemArquivo;
    }

    public void setImagemArquivo(String imagemArquivo) {
        this.imagemArquivo = imagemArquivo;
    }

    public byte[] getImagem() {
        return imagem;
    }

    public void setImagem(byte[] imagem) {
        this.imagem = imagem;
    }

    public String getImagemTipo() {
        return imagemTipo;
    }

    public void setImagemTipo(String imagemTipo) {
        this.imagemTipo = imagemTipo;
    }

    public Long getImagemVersao() {
        return imagemVersao;
    }

    public void setImagemVersao(Long imagemVersao) {
        this.imagemVersao = imagemVersao;
    }
}

package br.unitins.akmos.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

/** Kit de produtos montado para um objetivo (emagrecimento, beleza, treino...). */
@Entity
@Table(name = "kit")
public class Kit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    /** Selo que aparece na foto do kit (ex.: "Emagrecimento"). */
    @Column(nullable = false, length = 30)
    private String objetivo;

    @Column(length = 500)
    private String descricao;

    /**
     * Preço especial do kit. Se ficar vazio, o preço do kit é a soma dos produtos.
     * Ex.: o Kit AK Body tem preço oficial da Akmos.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal preco;

    /** Ex.: "Brindes: cartilha, shakeira e tableteira". */
    @Column(length = 150)
    private String brinde;

    /** Posição no carrossel do site (menor aparece primeiro). */
    @Column(nullable = false)
    private Integer ordem = 0;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToMany
    @JoinTable(name = "kit_produto",
            joinColumns = @JoinColumn(name = "id_kit"),
            inverseJoinColumns = @JoinColumn(name = "id_produto"))
    @OrderColumn(name = "posicao")
    private List<Produto> produtos = new ArrayList<>();

    @Column(name = "imagem_arquivo", length = 150)
    private String imagemArquivo;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "imagem", length = 5_000_000)
    private byte[] imagem;

    @Column(name = "imagem_tipo", length = 30)
    private String imagemTipo;

    @Column(name = "imagem_versao")
    private Long imagemVersao;

    public BigDecimal getSomaProdutos() {
        return produtos.stream()
                .map(Produto::getPreco)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Preço que aparece no site. */
    public BigDecimal getPrecoFinal() {
        return preco != null ? preco : getSomaProdutos();
    }

    /** O kit só está disponível se todos os produtos estiverem ativos e com estoque. */
    public boolean isDisponivel() {
        return Boolean.TRUE.equals(ativo)
                && !produtos.isEmpty()
                && produtos.stream().allMatch(Produto::isDisponivel);
    }

    public String getImagemUrl() {
        if (imagemTipo != null) {
            return "/api/kits/" + id + "/imagem?v=" + imagemVersao;
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

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
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

    public String getBrinde() {
        return brinde;
    }

    public void setBrinde(String brinde) {
        this.brinde = brinde;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public List<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<Produto> produtos) {
        this.produtos = produtos;
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

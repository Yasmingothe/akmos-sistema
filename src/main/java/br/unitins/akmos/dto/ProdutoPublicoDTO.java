package br.unitins.akmos.dto;

import java.math.BigDecimal;

import br.unitins.akmos.model.Produto;

/** Produto para o site público: mostra se tem ou não, mas não a quantidade exata do estoque. */
public record ProdutoPublicoDTO(
        Long id,
        String nome,
        String categoria,
        String descricao,
        BigDecimal preco,
        boolean disponivel,
        String imagemUrl) {

    public static ProdutoPublicoDTO valueOf(Produto p) {
        return new ProdutoPublicoDTO(
                p.getId(),
                p.getNome(),
                p.getCategoria(),
                p.getDescricao(),
                p.getPreco(),
                p.isDisponivel(),
                p.getImagemUrl());
    }
}

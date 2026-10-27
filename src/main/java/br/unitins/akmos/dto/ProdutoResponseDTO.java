package br.unitins.akmos.dto;

import java.math.BigDecimal;

import br.unitins.akmos.model.Produto;

/** Produto completo, para o painel do revendedor (inclui estoque). */
public record ProdutoResponseDTO(
        Long id,
        String nome,
        String categoria,
        String descricao,
        BigDecimal preco,
        Integer estoque,
        Boolean destaque,
        Boolean ativo,
        boolean disponivel,
        boolean fotoEnviada,
        String imagemUrl) {

    public static ProdutoResponseDTO valueOf(Produto p) {
        return new ProdutoResponseDTO(
                p.getId(),
                p.getNome(),
                p.getCategoria(),
                p.getDescricao(),
                p.getPreco(),
                p.getEstoque(),
                p.getDestaque(),
                p.getAtivo(),
                p.isDisponivel(),
                p.getImagemTipo() != null,
                p.getImagemUrl());
    }
}

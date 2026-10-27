package br.unitins.akmos.dto;

import java.math.BigDecimal;

import br.unitins.akmos.model.Produto;

/** Produto dentro de um kit. */
public record ItemKitDTO(Long id, String nome, BigDecimal preco, boolean disponivel) {

    public static ItemKitDTO valueOf(Produto p) {
        return new ItemKitDTO(p.getId(), p.getNome(), p.getPreco(), p.isDisponivel());
    }
}

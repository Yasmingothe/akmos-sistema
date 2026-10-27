package br.unitins.akmos.dto;

import java.math.BigDecimal;
import java.util.List;

import br.unitins.akmos.model.Kit;
import br.unitins.akmos.model.Produto;

/** Kit para o site público. */
public record KitPublicoDTO(
        Long id,
        String nome,
        String objetivo,
        String descricao,
        BigDecimal preco,
        /** true = preço especial definido pelo revendedor; false = soma dos produtos. */
        boolean precoEspecial,
        String brinde,
        boolean disponivel,
        String imagemUrl,
        List<String> itens) {

    public static KitPublicoDTO valueOf(Kit k) {
        return new KitPublicoDTO(
                k.getId(),
                k.getNome(),
                k.getObjetivo(),
                k.getDescricao(),
                k.getPrecoFinal(),
                k.getPreco() != null,
                k.getBrinde(),
                k.isDisponivel(),
                k.getImagemUrl(),
                k.getProdutos().stream().map(Produto::getNome).toList());
    }
}

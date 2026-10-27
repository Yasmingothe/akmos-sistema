package br.unitins.akmos.dto;

import java.math.BigDecimal;
import java.util.List;

import br.unitins.akmos.model.Kit;

/** Kit completo, para o painel do revendedor. */
public record KitResponseDTO(
        Long id,
        String nome,
        String objetivo,
        String descricao,
        BigDecimal preco,
        BigDecimal precoEspecial,
        BigDecimal somaProdutos,
        String brinde,
        Integer ordem,
        Boolean ativo,
        boolean disponivel,
        boolean fotoEnviada,
        String imagemUrl,
        List<ItemKitDTO> produtos) {

    public static KitResponseDTO valueOf(Kit k) {
        return new KitResponseDTO(
                k.getId(),
                k.getNome(),
                k.getObjetivo(),
                k.getDescricao(),
                k.getPrecoFinal(),
                k.getPreco(),
                k.getSomaProdutos(),
                k.getBrinde(),
                k.getOrdem(),
                k.getAtivo(),
                k.isDisponivel(),
                k.getImagemTipo() != null,
                k.getImagemUrl(),
                k.getProdutos().stream().map(ItemKitDTO::valueOf).toList());
    }
}

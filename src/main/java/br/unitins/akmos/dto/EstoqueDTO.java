package br.unitins.akmos.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Ajuste rápido de estoque.
 * Positivo = chegou mercadoria (ex.: 5). Negativo = saiu/vendeu (ex.: -1).
 */
public record EstoqueDTO(
        @NotNull(message = "Informe o ajuste do estoque.")
        Integer ajuste) {
}

package br.unitins.akmos.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados do formulário de produto no painel. */
public record ProdutoDTO(

        @NotBlank(message = "Informe o nome do produto.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @Size(max = 40, message = "A categoria deve ter no máximo 40 caracteres.")
        String categoria,

        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
        String descricao,

        @NotNull(message = "Informe o preço.")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
        @Digits(integer = 8, fraction = 2, message = "Preço inválido (use até 2 casas decimais).")
        BigDecimal preco,

        @NotNull(message = "Informe a quantidade em estoque.")
        @Min(value = 0, message = "O estoque não pode ser negativo.")
        Integer estoque,

        Boolean destaque,

        Boolean ativo) {
}

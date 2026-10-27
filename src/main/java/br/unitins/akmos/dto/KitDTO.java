package br.unitins.akmos.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/** Dados do formulário de kit no painel. */
public record KitDTO(

        @NotBlank(message = "Informe o nome do kit.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @NotBlank(message = "Informe o objetivo do kit (ex.: Emagrecimento).")
        @Size(max = 30, message = "O objetivo deve ter no máximo 30 caracteres.")
        String objetivo,

        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
        String descricao,

        /** Vazio = o preço do kit é a soma dos produtos. */
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
        @Digits(integer = 8, fraction = 2, message = "Preço inválido (use até 2 casas decimais).")
        BigDecimal preco,

        @Size(max = 150, message = "O brinde deve ter no máximo 150 caracteres.")
        String brinde,

        @Min(value = 0, message = "A ordem não pode ser negativa.")
        Integer ordem,

        Boolean ativo,

        @NotEmpty(message = "Escolha pelo menos um produto para o kit.")
        List<Long> idsProdutos) {
}

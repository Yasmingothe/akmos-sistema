package br.unitins.akmos.dto;

import br.unitins.akmos.model.TipoClique;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dados que a landing page envia a cada clique no WhatsApp. */
public record CliqueDTO(

        @NotNull(message = "O tipo é obrigatório (KIT, PRODUTO ou GERAL).")
        TipoClique tipo,

        @NotBlank(message = "O item é obrigatório.")
        @Size(max = 120, message = "O item deve ter no máximo 120 caracteres.")
        String item,

        @Size(max = 40, message = "A seção deve ter no máximo 40 caracteres.")
        String secao,

        @Pattern(regexp = "celular|computador", message = "O dispositivo deve ser 'celular' ou 'computador'.")
        String dispositivo) {
}

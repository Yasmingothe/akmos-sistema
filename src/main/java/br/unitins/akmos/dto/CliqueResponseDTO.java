package br.unitins.akmos.dto;

import java.time.LocalDateTime;

import br.unitins.akmos.model.Clique;
import br.unitins.akmos.model.TipoClique;

/** Clique devolvido pela API. */
public record CliqueResponseDTO(
        Long id,
        TipoClique tipo,
        String item,
        String secao,
        String dispositivo,
        LocalDateTime dataHora) {

    public static CliqueResponseDTO valueOf(Clique clique) {
        return new CliqueResponseDTO(
                clique.getId(),
                clique.getTipo(),
                clique.getItem(),
                clique.getSecao(),
                clique.getDispositivo(),
                clique.getDataHora());
    }
}

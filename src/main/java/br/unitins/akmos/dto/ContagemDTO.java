package br.unitins.akmos.dto;

/** Um nome e quantos cliques ele teve (ex.: "Kit AK Body" = 12). */
public record ContagemDTO(String nome, long total) {
}

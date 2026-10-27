package br.unitins.akmos.dto;

/** Bytes e tipo de uma foto guardada no banco. */
public record ImagemDTO(byte[] dados, String tipo) {
}

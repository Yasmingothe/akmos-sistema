package br.unitins.akmos.service;

import java.util.Set;

import br.unitins.akmos.exception.NegocioException;

/** Regras para as fotos enviadas pelo painel. */
public final class ImagemValidador {

    public static final long TAMANHO_MAXIMO = 3L * 1024 * 1024; // 3 MB
    private static final Set<String> TIPOS = Set.of("image/jpeg", "image/png", "image/webp");

    private ImagemValidador() {
    }

    public static void validar(byte[] dados, String tipo) {
        if (dados == null || dados.length == 0) {
            throw NegocioException.invalido("Escolha uma foto para enviar.");
        }
        if (tipo == null || !TIPOS.contains(tipo)) {
            throw NegocioException.invalido("Formato não aceito. Envie uma foto JPG, PNG ou WEBP.");
        }
        if (dados.length > TAMANHO_MAXIMO) {
            throw NegocioException.invalido("A foto deve ter no máximo 3 MB.");
        }
    }
}

package br.unitins.akmos.resource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import org.jboss.resteasy.reactive.multipart.FileUpload;

import br.unitins.akmos.dto.ImagemDTO;
import br.unitins.akmos.exception.NegocioException;
import jakarta.ws.rs.core.Response;

/** Ajuda os resources a ler a foto enviada e a devolver a foto guardada. */
final class ImagemUpload {

    private ImagemUpload() {
    }

    static byte[] ler(FileUpload arquivo) {
        if (arquivo == null || arquivo.uploadedFile() == null) {
            throw NegocioException.invalido("Escolha uma foto para enviar.");
        }
        try {
            return Files.readAllBytes(arquivo.uploadedFile());
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler a foto enviada.", e);
        }
    }

    static String tipo(FileUpload arquivo) {
        return arquivo == null ? null : arquivo.contentType();
    }

    /** Devolve a foto com cache longo: a URL muda (?v=...) sempre que a foto é trocada. */
    static Response responder(ImagemDTO imagem) {
        return Response.ok(imagem.dados())
                .type(imagem.tipo())
                .header("Cache-Control", "public, max-age=31536000")
                .build();
    }
}

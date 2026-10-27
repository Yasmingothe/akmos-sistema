package br.unitins.akmos.exception;

/**
 * Erro de regra do sistema, com a mensagem que aparece para o usuário
 * e o código HTTP (404 = não encontrado, 409 = conflito, 400 = dado inválido).
 */
public class NegocioException extends RuntimeException {

    private final int status;

    public NegocioException(String mensagem, int status) {
        super(mensagem);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public static NegocioException naoEncontrado(String mensagem) {
        return new NegocioException(mensagem, 404);
    }

    public static NegocioException conflito(String mensagem) {
        return new NegocioException(mensagem, 409);
    }

    public static NegocioException invalido(String mensagem) {
        return new NegocioException(mensagem, 400);
    }
}

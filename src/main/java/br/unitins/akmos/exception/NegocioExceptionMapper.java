package br.unitins.akmos.exception;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import br.unitins.akmos.dto.ErroDTO;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Transforma NegocioException em uma resposta JSON: {"mensagem": "..."}. */
public class NegocioExceptionMapper {

    @ServerExceptionMapper
    public RestResponse<ErroDTO> mapear(NegocioException e) {
        // O tipo JSON é fixado aqui porque o erro pode acontecer num endereço que devolve foto
        return RestResponse.ResponseBuilder
                .create(Response.Status.fromStatusCode(e.getStatus()), new ErroDTO(e.getMessage()))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}

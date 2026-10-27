package br.unitins.akmos.resource;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Usado pela tela de login do painel para conferir usuário e senha. */
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    SecurityIdentity identidade;

    public record UsuarioDTO(String usuario) {
    }

    @GET
    @Path("/eu")
    @RolesAllowed("admin")
    public UsuarioDTO eu() {
        return new UsuarioDTO(identidade.getPrincipal().getName());
    }
}

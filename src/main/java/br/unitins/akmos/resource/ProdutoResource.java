package br.unitins.akmos.resource;

import java.util.List;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import br.unitins.akmos.dto.EstoqueDTO;
import br.unitins.akmos.dto.ProdutoDTO;
import br.unitins.akmos.dto.ProdutoPublicoDTO;
import br.unitins.akmos.dto.ProdutoResponseDTO;
import br.unitins.akmos.service.ProdutoService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Produtos. Tudo exige login do revendedor, menos a lista pública (usada pelo site)
 * e as fotos.
 */
@Path("/api/produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
public class ProdutoResource {

    @Inject
    ProdutoService service;

    /** Site público. Ex.: GET /api/produtos/publico?destaque=true */
    @GET
    @Path("/publico")
    @PermitAll
    public List<ProdutoPublicoDTO> listarPublicos(@QueryParam("destaque") @DefaultValue("false") boolean destaque) {
        return service.listarPublicos(destaque);
    }

    @GET
    public List<ProdutoResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @GET
    @Path("/{id}")
    public ProdutoResponseDTO buscar(@PathParam("id") Long id) {
        return service.buscar(id);
    }

    @POST
    public Response criar(@Valid ProdutoDTO dto) {
        return Response.status(Status.CREATED).entity(service.criar(dto)).build();
    }

    @PUT
    @Path("/{id}")
    public ProdutoResponseDTO atualizar(@PathParam("id") Long id, @Valid ProdutoDTO dto) {
        return service.atualizar(id, dto);
    }

    /** Ajuste rápido de estoque: {"ajuste": 5} ou {"ajuste": -1}. */
    @PATCH
    @Path("/{id}/estoque")
    public ProdutoResponseDTO ajustarEstoque(@PathParam("id") Long id, @Valid EstoqueDTO dto) {
        return service.ajustarEstoque(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") Long id) {
        service.excluir(id);
        return Response.noContent().build();
    }

    /** Envio de foto (formulário multipart, campo "imagem"). */
    @PUT
    @Path("/{id}/imagem")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public ProdutoResponseDTO enviarImagem(@PathParam("id") Long id, @RestForm("imagem") FileUpload imagem) {
        return service.salvarImagem(id, ImagemUpload.ler(imagem), ImagemUpload.tipo(imagem));
    }

    /** Remove a foto enviada (volta para a foto padrão do site, se tiver). */
    @DELETE
    @Path("/{id}/imagem")
    public ProdutoResponseDTO removerImagem(@PathParam("id") Long id) {
        return service.removerImagem(id);
    }

    @GET
    @Path("/{id}/imagem")
    @PermitAll
    @Produces({ "image/jpeg", "image/png", "image/webp" })
    public Response imagem(@PathParam("id") Long id) {
        return ImagemUpload.responder(service.buscarImagem(id));
    }
}

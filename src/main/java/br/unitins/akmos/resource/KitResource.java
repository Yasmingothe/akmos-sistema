package br.unitins.akmos.resource;

import java.util.List;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import br.unitins.akmos.dto.KitDTO;
import br.unitins.akmos.dto.KitPublicoDTO;
import br.unitins.akmos.dto.KitResponseDTO;
import br.unitins.akmos.service.KitService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/** Kits. Tudo exige login, menos a lista pública (usada pelo site) e as fotos. */
@Path("/api/kits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
public class KitResource {

    @Inject
    KitService service;

    @GET
    @Path("/publico")
    @PermitAll
    public List<KitPublicoDTO> listarPublicos() {
        return service.listarPublicos();
    }

    @GET
    public List<KitResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @GET
    @Path("/{id}")
    public KitResponseDTO buscar(@PathParam("id") Long id) {
        return service.buscar(id);
    }

    @POST
    public Response criar(@Valid KitDTO dto) {
        return Response.status(Status.CREATED).entity(service.criar(dto)).build();
    }

    @PUT
    @Path("/{id}")
    public KitResponseDTO atualizar(@PathParam("id") Long id, @Valid KitDTO dto) {
        return service.atualizar(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") Long id) {
        service.excluir(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/imagem")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public KitResponseDTO enviarImagem(@PathParam("id") Long id, @RestForm("imagem") FileUpload imagem) {
        return service.salvarImagem(id, ImagemUpload.ler(imagem), ImagemUpload.tipo(imagem));
    }

    @DELETE
    @Path("/{id}/imagem")
    public KitResponseDTO removerImagem(@PathParam("id") Long id) {
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

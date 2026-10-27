package br.unitins.akmos.resource;

import java.time.LocalDate;
import java.util.List;

import br.unitins.akmos.dto.CliqueDTO;
import br.unitins.akmos.dto.CliqueResponseDTO;
import br.unitins.akmos.dto.ResumoDTO;
import br.unitins.akmos.service.CliqueService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/api/cliques")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CliqueResource {

    @Inject
    CliqueService service;

    /** Chamado pela landing page a cada clique em um botão de WhatsApp. Aberto ao público. */
    @POST
    @PermitAll
    public Response registrar(@Valid CliqueDTO dto) {
        CliqueResponseDTO criado = service.registrar(dto);
        return Response.status(Status.CREATED).entity(criado).build();
    }

    /** Lista os cliques mais recentes do período (só para o administrador). */
    @GET
    @RolesAllowed("admin")
    public List<CliqueResponseDTO> listar(
            @QueryParam("inicio") LocalDate inicio,
            @QueryParam("fim") LocalDate fim,
            @QueryParam("pagina") @DefaultValue("0") @Min(0) int pagina,
            @QueryParam("tamanho") @DefaultValue("50") @Min(1) @Max(500) int tamanho) {
        return service.listar(inicio, fim, pagina, tamanho);
    }

    /**
     * Relatório do período (só para o administrador).
     * Exemplo: GET /api/cliques/resumo?inicio=2026-10-01&fim=2026-10-31
     * Sem datas, mostra os últimos 30 dias.
     */
    @GET
    @Path("/resumo")
    @RolesAllowed("admin")
    public ResumoDTO resumo(
            @QueryParam("inicio") LocalDate inicio,
            @QueryParam("fim") LocalDate fim) {
        return service.resumo(inicio, fim);
    }
}

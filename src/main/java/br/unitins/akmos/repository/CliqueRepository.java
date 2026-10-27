package br.unitins.akmos.repository;

import java.time.LocalDate;
import java.util.List;

import br.unitins.akmos.dto.ContagemDTO;
import br.unitins.akmos.model.Clique;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CliqueRepository implements PanacheRepository<Clique> {

    private static final String PERIODO = "c.data between :inicio and :fim";

    public PanacheQuery<Clique> findByPeriodo(LocalDate inicio, LocalDate fim) {
        return find("data between ?1 and ?2", Sort.descending("dataHora"), inicio, fim);
    }

    public long contarNoPeriodo(LocalDate inicio, LocalDate fim) {
        return count("data between ?1 and ?2", inicio, fim);
    }

    /**
     * Conta os cliques agrupando por um campo da entidade.
     * O campo vem só de dentro desta classe (nunca do usuário), então não há risco de SQL injection.
     */
    private List<ContagemDTO> contarPor(String campo, String ordem, LocalDate inicio, LocalDate fim) {
        return contarPor(campo, ordem, "", inicio, fim);
    }

    private List<ContagemDTO> contarPor(String campo, String ordem, String filtroExtra,
            LocalDate inicio, LocalDate fim) {
        String jpql = "select c." + campo + ", count(c) from Clique c"
                + " where " + PERIODO + filtroExtra
                + " group by c." + campo
                + " order by " + ordem;

        return getEntityManager()
                .createQuery(jpql, Object[].class)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getResultList()
                .stream()
                .map(linha -> new ContagemDTO(
                        linha[0] == null ? "não informado" : linha[0].toString(),
                        ((Number) linha[1]).longValue()))
                .toList();
    }

    public List<ContagemDTO> contarPorTipo(LocalDate inicio, LocalDate fim) {
        return contarPor("tipo", "count(c) desc", inicio, fim);
    }

    /** Ranking só dos kits e produtos (os botões gerais aparecem juntos em "por tipo"). */
    public List<ContagemDTO> contarPorItem(LocalDate inicio, LocalDate fim) {
        return contarPor("item", "count(c) desc",
                " and c.tipo in (br.unitins.akmos.model.TipoClique.KIT, br.unitins.akmos.model.TipoClique.PRODUTO)",
                inicio, fim);
    }

    public List<ContagemDTO> contarPorSecao(LocalDate inicio, LocalDate fim) {
        return contarPor("secao", "count(c) desc", inicio, fim);
    }

    public List<ContagemDTO> contarPorDispositivo(LocalDate inicio, LocalDate fim) {
        return contarPor("dispositivo", "count(c) desc", inicio, fim);
    }

    public List<ContagemDTO> contarPorDia(LocalDate inicio, LocalDate fim) {
        return contarPor("data", "c.data asc", inicio, fim);
    }
}

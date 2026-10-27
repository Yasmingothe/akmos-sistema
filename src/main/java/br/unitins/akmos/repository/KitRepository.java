package br.unitins.akmos.repository;

import java.util.List;

import br.unitins.akmos.model.Kit;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KitRepository implements PanacheRepository<Kit> {

    public List<Kit> listarTodos() {
        return listAll(Sort.by("ordem").and("id"));
    }

    /** Kits que aparecem no carrossel do site, na ordem definida pelo revendedor. */
    public List<Kit> listarAtivos() {
        return list("ativo = true", Sort.by("ordem").and("id"));
    }

    /** Kits que usam um produto (para não deixar excluir um produto que está em kit). */
    public List<Kit> listarPorProduto(Long idProduto) {
        return list("select distinct k from Kit k join k.produtos p where p.id = ?1", idProduto);
    }
}

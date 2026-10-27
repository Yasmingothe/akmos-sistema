package br.unitins.akmos.repository;

import java.util.List;

import br.unitins.akmos.model.Produto;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProdutoRepository implements PanacheRepository<Produto> {

    public List<Produto> listarTodos() {
        return listAll(Sort.by("nome"));
    }

    /** Produtos que aparecem no site. */
    public List<Produto> listarAtivos(boolean somenteDestaques) {
        if (somenteDestaques) {
            return list("ativo = true and destaque = true", Sort.by("nome"));
        }
        return list("ativo = true", Sort.by("nome"));
    }
}

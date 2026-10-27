package br.unitins.akmos.service;

import java.util.List;
import java.util.stream.Collectors;

import br.unitins.akmos.dto.EstoqueDTO;
import br.unitins.akmos.dto.ImagemDTO;
import br.unitins.akmos.dto.ProdutoDTO;
import br.unitins.akmos.dto.ProdutoPublicoDTO;
import br.unitins.akmos.dto.ProdutoResponseDTO;
import br.unitins.akmos.exception.NegocioException;
import br.unitins.akmos.model.Kit;
import br.unitins.akmos.model.Produto;
import br.unitins.akmos.repository.KitRepository;
import br.unitins.akmos.repository.ProdutoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProdutoServiceImpl implements ProdutoService {

    @Inject
    ProdutoRepository repository;

    @Inject
    KitRepository kitRepository;

    @Override
    @Transactional
    public List<ProdutoPublicoDTO> listarPublicos(boolean somenteDestaques) {
        return repository.listarAtivos(somenteDestaques).stream()
                .map(ProdutoPublicoDTO::valueOf)
                .toList();
    }

    @Override
    @Transactional
    public List<ProdutoResponseDTO> listarTodos() {
        return repository.listarTodos().stream()
                .map(ProdutoResponseDTO::valueOf)
                .toList();
    }

    @Override
    @Transactional
    public ProdutoResponseDTO buscar(Long id) {
        return ProdutoResponseDTO.valueOf(buscarEntidade(id));
    }

    @Override
    @Transactional
    public ProdutoResponseDTO criar(ProdutoDTO dto) {
        Produto produto = new Produto();
        preencher(produto, dto);
        repository.persist(produto);
        return ProdutoResponseDTO.valueOf(produto);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO atualizar(Long id, ProdutoDTO dto) {
        Produto produto = buscarEntidade(id);
        preencher(produto, dto);
        return ProdutoResponseDTO.valueOf(produto);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO ajustarEstoque(Long id, EstoqueDTO dto) {
        Produto produto = buscarEntidade(id);
        int novoEstoque = produto.getEstoque() + dto.ajuste();
        if (novoEstoque < 0) {
            throw NegocioException.invalido(
                    "O estoque de " + produto.getNome() + " não pode ficar negativo (tem " + produto.getEstoque() + ").");
        }
        produto.setEstoque(novoEstoque);
        return ProdutoResponseDTO.valueOf(produto);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarEntidade(id);
        List<Kit> kits = kitRepository.listarPorProduto(id);
        if (!kits.isEmpty()) {
            String nomes = kits.stream().map(Kit::getNome).collect(Collectors.joining(", "));
            throw NegocioException.conflito("Não dá para excluir: o produto está no(s) kit(s) " + nomes
                    + ". Tire ele dos kits primeiro ou apenas desative o produto.");
        }
        repository.delete(produto);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO salvarImagem(Long id, byte[] dados, String tipo) {
        ImagemValidador.validar(dados, tipo);
        Produto produto = buscarEntidade(id);
        produto.setImagem(dados);
        produto.setImagemTipo(tipo);
        produto.setImagemVersao(System.currentTimeMillis());
        return ProdutoResponseDTO.valueOf(produto);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO removerImagem(Long id) {
        Produto produto = buscarEntidade(id);
        produto.setImagem(null);
        produto.setImagemTipo(null);
        produto.setImagemVersao(null);
        return ProdutoResponseDTO.valueOf(produto);
    }

    @Override
    @Transactional
    public ImagemDTO buscarImagem(Long id) {
        Produto produto = buscarEntidade(id);
        if (produto.getImagemTipo() == null) {
            throw NegocioException.naoEncontrado("Este produto não tem foto enviada.");
        }
        return new ImagemDTO(produto.getImagem(), produto.getImagemTipo());
    }

    private Produto buscarEntidade(Long id) {
        Produto produto = repository.findById(id);
        if (produto == null) {
            throw NegocioException.naoEncontrado("Produto não encontrado.");
        }
        return produto;
    }

    private void preencher(Produto produto, ProdutoDTO dto) {
        produto.setNome(dto.nome().trim());
        produto.setCategoria(vazioParaNulo(dto.categoria()));
        produto.setDescricao(vazioParaNulo(dto.descricao()));
        produto.setPreco(dto.preco());
        produto.setEstoque(dto.estoque());
        produto.setDestaque(Boolean.TRUE.equals(dto.destaque()));
        produto.setAtivo(dto.ativo() == null || dto.ativo());
    }

    static String vazioParaNulo(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
